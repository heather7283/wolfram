package io.github.heather7283.wolfram.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.utils.CIDR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class SettingsPopup {
    data object Inactive : SettingsPopup()
    data class Cidr(
        val title: String,
        val ip: String?,
        val prefix: Int?,
        val onConfirm: (cidr: CIDR) -> Unit
    ) : SettingsPopup()
}

data class SettingsUiState(
    val popup: SettingsPopup = SettingsPopup.Inactive,
    val statsEndpointModified: Boolean = false,
    val statsEndpointText: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings = settingsRepository.settingsFlow

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun openCidrPopup(
        title: String,
        ip: String? = null,
        prefix: Int? = null,
        onConfirm: (cidr: CIDR) -> Unit,
    ) {
        _uiState.update {
            val popup = SettingsPopup.Cidr(
                title = title,
                ip = ip,
                prefix = prefix,
                onConfirm = onConfirm,
            )
            it.copy(popup = popup)
        }
    }
    fun closePopup() {
        _uiState.update { it.copy(popup = SettingsPopup.Inactive) }
    }

    fun removeVpnAddress(address: CIDR) = viewModelScope.launch {
        settingsRepository.removeVpnAddress(address).onLeft {
            Timber.e(it)
        }
    }
    fun addVpnAddress(address: CIDR) = viewModelScope.launch {
        settingsRepository.addVpnAddress(address).onLeft {
            Timber.e(it)
        }.onRight {
            closePopup()
        }
    }

    fun removeVpnRoute(address: CIDR) = viewModelScope.launch {
        settingsRepository.removeVpnRoute(address).onLeft {
            Timber.e(it)
        }
    }
    fun addVpnRoute(address: CIDR) = viewModelScope.launch {
        settingsRepository.addVpnRoute(address).onLeft {
            Timber.e(it)
        }.onRight {
            closePopup()
        }
    }

    fun setStatsEnabled(b: Boolean) = viewModelScope.launch {
        settingsRepository.setStatsEnabled(b)
    }

    fun onStatsEndpointValueChange(s: String) = _uiState.update {
        it.copy(statsEndpointModified = true, statsEndpointText = s)
    }
    fun onStatsEndpointSave() = viewModelScope.launch {
        settingsRepository.setStatsEndpoint(_uiState.value.statsEndpointText).onRight {
            _uiState.update {
                it.copy(statsEndpointModified = false)
            }
        }
    }
}
