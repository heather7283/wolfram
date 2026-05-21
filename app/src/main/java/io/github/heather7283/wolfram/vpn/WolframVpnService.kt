package io.github.heather7283.wolfram.vpn

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
import java.util.Collections.emptyMap
import kotlin.io.bufferedWriter
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class WolframVpnService : VpnService() {
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

    private val _stats: MutableStateFlow<Either<Throwable, XrayStats>> =
        MutableStateFlow(Either.Right(XrayStats(emptyMap(), emptyMap())))
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
                "start" -> startVpn(
                    intent?.getStringExtra("config")!!,
                    intent?.getStringExtra("assetsDir")!!,
                )
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

    fun startVpn(config: String, assetsDir: String) {
        Timber.d("startVpn called")
        if (_running.value) {
            Timber.w("startVpn called when VPN is already running")
            return
        }

        val tunFd = buildTunInterface()
        if (tunFd == null) {
            Timber.e("failed to build TUN interface")
            return
        }

        launchCore(tunFd, config, assetsDir)
        startForeground(NOTIF_ID, buildNotification())
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

    private fun launchCore(tunFd: ParcelFileDescriptor, config: String, assetsDir: String) {
        val sockName = "io.github.heather7283.wolfram.sock"
        val sock = LocalServerSocket(sockName)

        val wrapper = applicationInfo.nativeLibraryDir + "/libxray-wrapper.so"
        val binary = applicationInfo.nativeLibraryDir + "/libxray.so"

        try {
            process = ProcessBuilder(wrapper, sockName, binary, "run")
                .apply { environment()["XRAY_LOCATION_ASSET"] = assetsDir }
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
                    Path(config).bufferedReader().use { reader ->
                        reader.copyTo(writer)
                        writer.flush()
                    }
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
            val http = OkHttpClient.Builder()
                .callTimeout(1.seconds)
                .build()

            do {
                delay(5.seconds) // TODO: configurable

                _stats.update {
                    Either.catch {
                        val req = Request.Builder()
                            .url("http://127.0.0.1:54321/debug/vars") // TODO: configurable
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
                    }
                }

                _stats.value.onLeft {
                    Timber.e(it, "could not fetch stats")
                }.onRight {
                    Timber.d("stats: ${it}")
                }
            } while (_running.value)
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
            _logs.emit("[process exited with code $rc]")
        }
    }

    private fun buildTunInterface(): ParcelFileDescriptor? {
        return Builder()
            .setSession("Wolfram")
            .addAddress("10.20.30.1", 24)
            .addRoute("0.0.0.0", 0)
            .addDisallowedApplication("io.github.heather7283.wolfram") // important loop protection
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
