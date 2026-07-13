package io.github.heather7283.wolfram.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileEntity
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface DialogState {
    data object Hidden : DialogState
    data object Add : DialogState
    data class Edit(val target: GeoFile) : DialogState
}

data class GeoFileUiState(
    val dialog: DialogState = DialogState.Hidden,
    val downloadingNames: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

@HiltViewModel
class GeoFileViewModel @Inject constructor(
    private val repo: GeoFileRepository
) : ViewModel() {

    val geoFilesEntity: StateFlow<List<GeoFile>> = repo.geoFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(GeoFileUiState())
    val uiState: StateFlow<GeoFileUiState> = _uiState.asStateFlow()

    fun showAddDialog() {
        _uiState.update { it.copy(dialog = DialogState.Add) }
    }

    fun showEditDialog(geoFile: GeoFile) {
        _uiState.update { it.copy(dialog = DialogState.Edit(geoFile)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = DialogState.Hidden) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun add(name: String, url: String) {
        viewModelScope.launch {
            repo.add(name.trim(), url.trim()).onLeft { err ->
                Timber.e(err)
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to add geo file") }
            }
        }
        dismissDialog()
    }

    fun edit(oldName: String, name: String, url: String) {
        viewModelScope.launch {
            repo.modify(oldName, name.trim(), url.trim()).onLeft { err ->
                Timber.e(err)
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to edit geo file") }
            }
        }
        dismissDialog()
    }

    fun download(name: String) {
        if (_uiState.value.downloadingNames.contains(name)) return
        _uiState.update { it.copy(downloadingNames = it.downloadingNames + name) }
        viewModelScope.launch {
            repo.download(name)
                .onLeft { err ->
                    Timber.e(err)
                    _uiState.update {
                        it.copy(errorMessage = err.message ?: "Download failed for ${name}")
                    }
                }
            _uiState.update { it.copy(downloadingNames = it.downloadingNames - name) }
        }
    }

    fun delete(name: String) {
        viewModelScope.launch {
            repo.delete(name).onLeft { err ->
                Timber.e(err)
                _uiState.update {
                    it.copy(errorMessage = err.message ?: "Failed to delete geofile ${name}")
                }
            }
        }
    }
}
