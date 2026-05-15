package io.github.heather7283.wolfram.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Binder
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

@AndroidEntryPoint
class WolframVpnService : VpnService() {
    private val binder = WolframVpnServiceBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var fakeProcess: Job? = null

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
                "start" -> startVpn()
                "stop" -> stopVpn()
                else -> { Timber.e("Unknown action: $it") }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("onDestroy called")
        fakeProcess?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    fun startVpn() {
        Timber.d("startVpn called")
        if (_running.value) {
            Timber.w("startVpn called when VPN is already running")
            return
        }

        startForeground(NOTIF_ID, buildNotification())
        fakeProcess = scope.launch { startFakeProcess() }
    }

    fun stopVpn() {
        Timber.d("stopVpn called")
        if (!_running.value) {
            Timber.w("stopVpn called when VPN is not running")
            return
        }

        fakeProcess?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel("vpn", "VPN", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return NotificationCompat.Builder(this, "vpn")
            .setContentTitle("Wolfram VPN")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private suspend fun startFakeProcess() {
        Timber.d("fakeProcess is starting!")
        _running.update { true }

        try {
            var n = 0
            while (n < 60) {
                delay(1000)
                Timber.d("fakeProcess is alive! n = $n")
                _logs.emit("fake log from fakeProcess, n = $n")
                n += 1
            }

            Timber.d("fakeProcess is exiting!")
            _logs.emit("fakeProcess is exiting!")
            _running.update { false }
        } catch (e: CancellationException) {
            Timber.d("fakeProcess was cancelled externally")
            _logs.emit("fakeProcess was cancelled externally")
            _running.update { false }
            throw e
        }
    }
}
