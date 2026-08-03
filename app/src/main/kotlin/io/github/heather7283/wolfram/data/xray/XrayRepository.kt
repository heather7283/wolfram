package io.github.heather7283.wolfram.data.xray

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.heather7283.wolfram.service.WolframVpnService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XrayRepository @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    private val _logs = MutableSharedFlow<String>(replay = 100)
    val logs = _logs.asSharedFlow()

    private val _stats = MutableStateFlow<XrayStatsOption>(XrayStatsOption.Idle)
    val stats = _stats.asStateFlow()

    suspend fun postLogLine(line: String) = _logs.emit(line)
    suspend fun postStats(stats: XrayStatsOption) = _stats.emit(stats)
    suspend fun postRunning(running: Boolean) = _running.emit(running)

    private fun getIntent(extra: Map<String, String> = emptyMap()): Intent {
         return Intent(ctx, WolframVpnService::class.java).apply {
             extra.forEach { (k, v) -> putExtra(k, v) }
        }
    }

    fun startVpn() {
        @OptIn(ExperimentalCoroutinesApi::class)
        _logs.resetReplayCache()
        ContextCompat.startForegroundService(ctx, getIntent(mapOf("action" to "start")))
    }

    fun stopVpn() {
        Timber.d("stopVpn called")
        ctx.startService(getIntent(mapOf("action" to "stop")))
    }

    fun toggleVpn() {
        Timber.d("toggleVpn called")
        if (_running.value) stopVpn() else startVpn()
    }
}
