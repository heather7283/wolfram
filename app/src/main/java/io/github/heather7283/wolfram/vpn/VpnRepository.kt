package io.github.heather7283.wolfram.vpn

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
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

    private fun getIntent(act: String? = null) = Intent(ctx, WolframVpnService::class.java).apply {
        if (act != null) {
            putExtra("action", act)
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

    fun startVpn() {
        Timber.d("startVpn called")
        ContextCompat.startForegroundService(ctx, getIntent("start"))
    }

    fun stopVpn() {
        Timber.d("stopVpn called")
        ctx.startService(getIntent("stop"))
    }
}
