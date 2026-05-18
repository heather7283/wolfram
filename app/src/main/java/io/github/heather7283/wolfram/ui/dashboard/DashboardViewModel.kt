package io.github.heather7283.wolfram.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.ConfigFilesRepository
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import io.github.heather7283.wolfram.vpn.VpnRepository
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vpnRepository: VpnRepository,
    private val configFilesRepository: ConfigFilesRepository,
    private val geoFileRepository: GeoFileRepository,
) : ViewModel() {
    val running = vpnRepository.running

    // TODO: check and select
    fun startVpn() = vpnRepository.startVpn(
        configFilesRepository.getConfigFiles().first(),
        geoFileRepository.geoFilesDir,
    )
    fun stopVpn() = vpnRepository.stopVpn()
}