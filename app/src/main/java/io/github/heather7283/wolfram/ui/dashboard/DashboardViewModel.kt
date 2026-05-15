package io.github.heather7283.wolfram.ui.dashboard

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.vpn.VpnRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vpnRepository: VpnRepository
) : ViewModel() {
    val running = vpnRepository.running

    val log = mutableStateListOf<String>()

    init {
        viewModelScope.launch {
            vpnRepository.logs.collect { line ->
                log.add(line)
                if (log.size > 500) {
                    log.removeAt(0)
                }
            }
        }
    }

    fun startVpn() = vpnRepository.startVpn()
    fun stopVpn() = vpnRepository.stopVpn()
}