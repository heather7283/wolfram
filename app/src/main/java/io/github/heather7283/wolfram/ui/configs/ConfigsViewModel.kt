package io.github.heather7283.wolfram.ui.configs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.ConfigsRepository
import io.github.heather7283.wolfram.data.XrayConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ConfigsUiState(
    val configs: List<XrayConfig> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)

@HiltViewModel
class ConfigsViewModel @Inject constructor(
    private val configsRepository: ConfigsRepository,
) : ViewModel() {
    val uiState: StateFlow<ConfigsUiState> = configsRepository.getConfigsFlow()
        .map {
            ConfigsUiState(configs = it)
        }
        .catch {
            Timber.e(it)
            emit(ConfigsUiState(configs = emptyList()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConfigsUiState(),
        )

    fun refresh() {
        viewModelScope.launch { configsRepository.refreshConfigs() }
    }

    fun deleteConfig(config: XrayConfig) {
        viewModelScope.launch { configsRepository.deleteConfig(config) }
    }
}
