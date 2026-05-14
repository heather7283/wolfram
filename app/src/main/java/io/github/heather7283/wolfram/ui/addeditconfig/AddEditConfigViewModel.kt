package io.github.heather7283.wolfram.ui.addeditconfig

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.ConfigFilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AddEditConfigUiState(
    val name: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isModified: Boolean = false,
)

@HiltViewModel
class AddEditConfigViewModel @Inject constructor(
    private val configFilesRepository: ConfigFilesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val configName: String? = savedStateHandle["configName"]

    private val _uiState = MutableStateFlow(AddEditConfigUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (configName != null) {
            loadConfig(configName)
        }
    }

    private fun loadConfig(configName: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            configFilesRepository.getConfigFile(configName).onLeft { e ->
                Timber.e(e)
            }.onRight { config ->
                configFilesRepository.getConfigFileText(config).onLeft { e ->
                    Timber.e(e)
                }.onRight { content ->
                    _uiState.update { state ->
                        state.copy(name = config.name, content = content, isLoading = false)
                    }
                }
            }
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            configFilesRepository.saveOrUpdateConfigFile(_uiState.value.name, _uiState.value.content).onLeft { e ->
                Timber.e(e)
            }.onRight {
                _uiState.update { state -> state.copy(isModified = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { state -> state.copy(name = name, isModified = true) }
    }

    fun updateContent(content: String) {
        _uiState.update { state -> state.copy(content = content, isModified = true) }
    }
}
