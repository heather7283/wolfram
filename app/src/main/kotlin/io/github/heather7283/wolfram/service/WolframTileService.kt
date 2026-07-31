package io.github.heather7283.wolfram.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import arrow.core.getOrElse
import dagger.hilt.android.AndroidEntryPoint
import io.github.heather7283.wolfram.WolframActivity
import io.github.heather7283.wolfram.data.config.XrayConfigRepository
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.data.xray.XrayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WolframTileService : TileService() {
    @Inject lateinit var xrayRepository: XrayRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var configRepository: XrayConfigRepository

    private var job: Job? = null

    private data class TileState(
        val state: Int,
        val subtitle: String,
    )

    // Called when the user adds your tile.
    override fun onTileAdded() {
        Timber.d("onTileAdded")
        super.onTileAdded()
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        Timber.d("onStartListening")
        super.onStartListening()

        job = CoroutineScope(Dispatchers.Main).launch {
            combine(xrayRepository.running, settingsRepository.settingsFlow) { running, settings ->
                TileState(
                    state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
                    subtitle = configRepository
                        .getNameById(settings.activeConfigId)
                        .getOrElse { "???" },
                )
            }.collect {
                qsTile.apply {
                    state = it.state
                    if (Build.VERSION.SDK_INT >= 29) {
                        subtitle = it.subtitle
                    }
                    updateTile()
                }
            }
        }
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        Timber.d("onStopListening")
        super.onStopListening()

        job?.cancel()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        Timber.d("onClick")

        val intent = Intent(this, WolframActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val unlockAndThen = fun(f: () -> Unit) {
            if (isSecure) f() else unlockAndRun(f)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            unlockAndThen { startActivityAndCollapse(pendingIntent) }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            unlockAndThen { startActivityAndCollapse(intent) }
        }
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        Timber.d("onTileRemoved")
        super.onTileRemoved()
    }
}
