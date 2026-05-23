package io.github.heather7283.wolfram.ui.addeditconfig

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AddEditConfigUiState(
    val name: String = "",
    val content: String = "",
    val isModified: Boolean = false,
)

@HiltViewModel
class AddEditConfigViewModel @Inject constructor(
    private val xrayConfigRepository: XrayConfigRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var id: Long? = savedStateHandle.get<Long?>("configId").let {
        if (it == null || it < 0) { null } else { it }
    }

    private val _uiState = MutableStateFlow(AddEditConfigUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (id != null) {
            loadConfig(id!!)
        }
    }

    private fun loadConfig(id: Long) {
        viewModelScope.launch {
            xrayConfigRepository.getById(id).onLeft { e ->
                Timber.e(e, "could not get config with id ${id}")
            }.onRight { config ->
                _uiState.update { state ->
                    state.copy(name = config.name, content = config.text)
                }
            }
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            val name = _uiState.value.name
            val text = _uiState.value.content

            if (id == null) {
                xrayConfigRepository.create(name, text).onLeft {
                    Timber.e(it, "failed to create config")
                }.onRight {
                    id = it
                    _uiState.update { it.copy(isModified = false) }
                }
            } else {
                xrayConfigRepository.update(id!!, name, text).onLeft {
                    Timber.e(it, "failed to save config")
                }.onRight {
                    _uiState.update { it.copy(isModified = false) }
                }
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
