package io.github.heather7283.wolfram

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import io.github.heather7283.wolfram.data.xray.XrayRepository
import jakarta.inject.Inject
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class WolframApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.i("Debug logging attached")
        }
    }
}
