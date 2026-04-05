package io.github.heather7283.wolfram.ui.configs

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.ConfigsRepository
import io.github.heather7283.wolfram.data.XrayConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ConfigsUiState(
    val configs: List<XrayConfig> = emptyList(),
)

@HiltViewModel
class ConfigsViewModel @Inject constructor(
    private val configsRepository: ConfigsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigsUiState())
    val uiState: StateFlow<ConfigsUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update { currentState ->
            currentState.copy(
                configs = configsRepository.getConfigs()
            )
        }
    }
}
