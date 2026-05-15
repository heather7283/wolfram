package io.github.heather7283.wolfram.ui.dashboard

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.ConfigFile
import io.github.heather7283.wolfram.data.ConfigFilesRepository
import io.github.heather7283.wolfram.vpn.VpnRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vpnRepository: VpnRepository,
    private val configFilesRepository: ConfigFilesRepository,
) : ViewModel() {
    val running = vpnRepository.running

    // TODO: check and select
    fun startVpn() = vpnRepository.startVpn(configFilesRepository.getConfigFiles().first())
    fun stopVpn() = vpnRepository.stopVpn()
}