package io.github.heather7283.wolfram.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.LocalServerSocket
import android.net.VpnService
import android.os.Binder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import arrow.core.Either
import arrow.core.flatMap
import dagger.hilt.android.AndroidEntryPoint
import io.github.heather7283.wolfram.R
import io.github.heather7283.wolfram.data.config.XrayConfigRepository
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import io.github.heather7283.wolfram.data.settings.Settings
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.data.xray.XrayStats
import io.github.heather7283.wolfram.data.xray.XrayStatsOption
import io.github.heather7283.wolfram.data.xray.parseXrayStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class WolframVpnService : VpnService() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var geoFileRepository: GeoFileRepository
    @Inject lateinit var xrayConfigRepository: XrayConfigRepository

    private val binder = WolframVpnServiceBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var process: Process? = null

    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    private val _logs = MutableSharedFlow<String>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val logs = _logs.asSharedFlow()

    private val _stats = MutableStateFlow<XrayStatsOption>(XrayStatsOption.Idle)
    val stats = _stats.asStateFlow()

    private val NOTIF_ID = 67;

    inner class WolframVpnServiceBinder : Binder() {
        fun getService() = this@WolframVpnService
    }

    override fun onBind(intent: Intent) = binder
    override fun onRebind(intent: Intent) = Unit
    override fun onUnbind(intent: Intent) = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand action ${intent?.getStringExtra("action")}")
        intent?.getStringExtra("action").also {
            when (it) {
                "start" -> startVpn()
                "stop" -> stopVpn()
                else -> { Timber.e("Unknown action: $it") }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Timber.d("onDestroy called")
        stopVpn()
        scope.cancel()
        super.onDestroy()
    }

    fun startVpn() {
        // TODO: should this be a coroutine? I can't access the db otherwise
        CoroutineScope(Dispatchers.Default).launch {
            Timber.d("startVpn called")
            if (_running.value) {
                Timber.w("startVpn called when VPN is already running")
                return@launch
            }

            val settings = settingsRepository.getSettings()
            val configId = settings.activeConfigId
            val config = xrayConfigRepository.getById(configId).onLeft {
                Timber.e(it, "xray config with id ${configId} not found")
                return@launch
            }.getOrNull()!!

            val tunFd = buildTunInterface()
            if (tunFd == null) {
                Timber.e("failed to build TUN interface")
                return@launch
            }

            launchCore(tunFd, config.text, settings)
            startForeground(NOTIF_ID, buildNotification())
        }
    }

    fun stopVpn() {
        Timber.d("stopVpn called")
        if (!_running.value) {
            Timber.w("stopVpn called when VPN is not running")
            return
        }

        process?.destroy()
        process = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun launchCore(tunFd: ParcelFileDescriptor, configText: String, settings: Settings) {
        val sockName = "io.github.heather7283.wolfram.sock"
        val sock = LocalServerSocket(sockName)

        val wrapper = applicationInfo.nativeLibraryDir + "/libxray-wrapper.so"
        val binary = applicationInfo.nativeLibraryDir + "/libxray.so"

        val assetsDir = geoFileRepository.geoFilesDir

        try {
            process = ProcessBuilder(wrapper, sockName, binary, "run")
                .apply { environment()["XRAY_LOCATION_ASSET"] = assetsDir.pathString }
                .redirectErrorStream(true)
                .start()
            _running.update { true }
        } catch (e: Exception) {
            Timber.e(e, "failed to start child process")
            sock.close()
            return
        }

        scope.launch {
            try {
                process?.outputStream?.bufferedWriter()?.use { writer ->
                    writer.write(configText)
                    writer.flush()
                }
            } catch (e: Exception) {
                Timber.e(e, "failed to feed config to xray")
            }
        }
        scope.launch {
            try {
                sock.use { server ->
                    server.accept().use { client ->
                        client.setFileDescriptorsForSend(arrayOf(tunFd.fileDescriptor))
                        client.outputStream.apply {
                            write(67)
                            flush()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "failed to send tun fd to child")
            } finally {
                tunFd.close()
            }
        }

        scope.launch {
            if (!settings.statsEnabled) {
                _stats.update { XrayStatsOption.Idle }
                return@launch
            }
            _stats.update { XrayStatsOption.Success(XrayStats()) }

            val http = OkHttpClient.Builder()
                .callTimeout(1.seconds)
                .build()

            while (true) {
                delay(settings.statsPollInterval.seconds)
                if (!_running.value) {
                    break
                }
                _stats.update {
                    Either.catch {
                        val req = Request.Builder()
                            .url("http://${settings.statsEndpoint}/debug/vars")
                            .build()

                        http.newCall(req).execute().use { resp ->
                            if (!resp.isSuccessful) {
                                throw IOException("HTTP ${resp.code}")
                            }
                            resp.body.use { it.string() }
                        }
                    }.flatMap { body ->
                        // TODO: use grpc instead, should be more efficient? not like it matters
                        parseXrayStats(body)
                    }.fold(
                        ifLeft = { XrayStatsOption.Error(it) },
                        ifRight = { XrayStatsOption.Success(it) }
                    )
                }
            }
        }
        scope.launch {
            try {
                process?.inputStream?.bufferedReader()?.lineSequence()?.forEach {
                    line -> _logs.emit(line)
                }
            } catch (_: InterruptedIOException) {
                // this is fine
            }
        }
        scope.launch {
            val rc = process?.waitFor() ?: -1
            _running.update { false }
            _stats.update { XrayStatsOption.Idle }
            _logs.emit("[process exited with code $rc]")
        }
    }

    private fun buildTunInterface(): ParcelFileDescriptor? {
        val settings = settingsRepository.getSettings()

        return Builder()
            .setSession("Wolfram")
            .apply { settings.vpnAddresses.forEach { addAddress(it.ip, it.prefix) } }
            .apply { settings.vpnRoutes.forEach { addRoute(it.ip, it.prefix) } }
            .apply { settings.dnsAddresses.forEach { addDnsServer(it) } }
            .apply {
                if (settings.selectedAppsIsWhitelist) {
                    settings.selectedApps
                        .filterNot { it == "io.github.heather7283.wolfram" } // loop protection
                        .forEach(::addAllowedApplication)
                } else {
                    settings.selectedApps.forEach(::addDisallowedApplication)
                    addDisallowedApplication("io.github.heather7283.wolfram") // loop protection
                }
            }
            .establish()
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel("vpn", "VPN", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return NotificationCompat.Builder(this, "vpn")
            .setContentTitle("Wolfram VPN")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
