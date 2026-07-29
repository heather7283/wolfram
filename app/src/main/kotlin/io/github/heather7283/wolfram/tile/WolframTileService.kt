package io.github.heather7283.wolfram.tile

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import io.github.heather7283.wolfram.WolframActivity
import timber.log.Timber

class WolframTileService : TileService() {
    // Called when the user adds your tile.
    override fun onTileAdded() {
        Timber.d("onTileAdded")
        super.onTileAdded()
    }
    // Called when your app can update your tile.
    override fun onStartListening() {
        Timber.d("onStartListening")
        super.onStartListening()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        Timber.d("onStopListening")
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        Timber.d("onClick")

        val intent = Intent(this, WolframActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val unlockAndThen = fun(f: () -> Unit) {
            if (isSecure) f() else unlockAndRun(f)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
