package io.github.heather7283.wolfram.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
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
