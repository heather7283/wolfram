package io.github.heather7283.wolfram.ui.logs

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.vpn.VpnRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val vpnRepository: VpnRepository
) : ViewModel() {
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
}