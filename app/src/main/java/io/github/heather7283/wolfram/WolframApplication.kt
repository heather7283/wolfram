package io.github.heather7283.wolfram

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import io.github.heather7283.wolfram.vpn.VpnRepository
import jakarta.inject.Inject
import timber.log.Timber
import timber.log.Timber.DebugTree

class VpnServiceLifecycleObserver @Inject constructor(
    private val repo: VpnRepository
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) = repo.bind()
    override fun onStop(owner: LifecycleOwner)  = repo.unbind()
}

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
}
