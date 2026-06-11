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
    @Inject lateinit var observer: VpnServiceLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.i("Debug logging attached")
        }
    }

    class VpnServiceLifecycleObserver @Inject constructor(
        private val repo: XrayRepository
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) = repo.bind()
        override fun onStop(owner: LifecycleOwner)  = repo.unbind()
    }
}
