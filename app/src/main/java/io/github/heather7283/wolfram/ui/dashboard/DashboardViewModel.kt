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
) : ViewModel() {
    val running = xrayRepository.running
    val stats = xrayRepository.stats

    fun startVpn() = xrayRepository.startVpn()
    fun stopVpn() = xrayRepository.stopVpn()
}
