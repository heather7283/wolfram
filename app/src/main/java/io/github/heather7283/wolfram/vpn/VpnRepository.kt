package io.github.heather7283.wolfram.vpn

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.heather7283.wolfram.data.ConfigFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.name
import kotlin.io.path.pathString

@Singleton
class VpnRepository @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private var service: WolframVpnService? = null

    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    private val _logs = MutableSharedFlow<String>(replay = 100)
    val logs = _logs.asSharedFlow()

    private var mirrorJobs: List<Job> = emptyList()
    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Timber.d("connection onServiceConnected")
            service = (binder as WolframVpnService.WolframVpnServiceBinder).getService()

            mirrorJobs.forEach { it.cancel() }
            mirrorJobs = listOf(
                connectionScope.launch {
                    service?.running?.collect { v -> _running.update { v } }
                },
                connectionScope.launch {
                    service?.logs?.collect { l -> _logs.emit(l) }
                },
            )
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.d("connection onServiceDisconnected")
            mirrorJobs.forEach { it.cancel() }
            mirrorJobs = emptyList()
            service = null
        }
    }

    private fun getIntent(extra: Map<String, String> = emptyMap()): Intent {
         return Intent(ctx, WolframVpnService::class.java).apply {
             extra.forEach { (k, v) -> putExtra(k, v) }
        }
    }

    fun bind() {
        Timber.d("bind() called")
        getIntent().also {
            ctx.bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        Timber.d("unbind() called")
        ctx.unbindService(connection)
    }

    fun startVpn(configFile: ConfigFile) {
        Timber.d("startVpn called with configFile ${configFile.name} at ${configFile.path}")
        ContextCompat.startForegroundService(ctx, getIntent(mapOf(
            "action" to "start",
            "config" to configFile.path.pathString,
        )))
    }

    fun stopVpn() {
        Timber.d("stopVpn called")
        ctx.startService(getIntent(mapOf("action" to "stop")))
    }
}
