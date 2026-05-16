package io.github.heather7283.wolfram.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.ParcelFileDescriptor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.heather7283.wolfram.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import timber.log.Timber
import java.io.InterruptedIOException
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.readSymbolicLink

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
                "start" -> startVpn(intent?.getStringExtra("config")!!)
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

    fun startVpn(config: String) {
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

        launchCore(tunFd, config)
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

    private fun launchCore(fd: ParcelFileDescriptor, config: String) {
        val wrapper = applicationInfo.nativeLibraryDir + "/libcoreWrapper.so"
        val binary = applicationInfo.nativeLibraryDir + "/libxray.so"
        process = ProcessBuilder(wrapper, binary, "run", "--config", config)
            .redirectErrorStream(true)
            .start()
        _running.update { true }

        if (!sendFd(fd.fd)) {
            Timber.e("failed to send TUN fd to xray")
        }

        scope.launch {
            try {
                process?.inputStream?.bufferedReader()?.lineSequence()?.forEach {
                        line -> _logs.emit(line)
                }
            } catch (_: InterruptedIOException) {
                // TODO: better way to do this?
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

    private external fun sendFd(fd: Int): Boolean

    companion object {
        init {
            System.loadLibrary("wolfram")
        }
    }
}
