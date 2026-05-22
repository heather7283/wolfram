package io.github.heather7283.wolfram.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigRepository
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import io.github.heather7283.wolfram.vpn.XrayRepository
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val xrayRepository: XrayRepository,
    private val xrayConfigRepository: XrayConfigRepository,
    private val geoFileRepository: GeoFileRepository,
) : ViewModel() {
    val running = xrayRepository.running
    val stats = xrayRepository.stats

    // TODO: check and select
    fun startVpn() = xrayRepository.startVpn(
        xrayConfigRepository.getConfigFiles().first(),
        geoFileRepository.geoFilesDir,
    )
    fun stopVpn() = xrayRepository.stopVpn()
}
