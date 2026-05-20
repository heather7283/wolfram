package io.github.heather7283.wolfram.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject



@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val geoFileRepository: GeoFileRepository,
) : ViewModel() {
    val geoFiles = geoFileRepository.geoFiles

    fun add(name: String, url: String) {
        viewModelScope.launch {
            geoFileRepository.add(name, url).onLeft {
                Timber.e("failed to add geofile: ${it}")
            }
        }
    }

    fun update(old: GeoFile, newName: String, newUrl: String) {
        viewModelScope.launch {
            geoFileRepository.modify(old, newName, newUrl).onLeft {
                Timber.e("failed to update geofile: ${it}")
            }
        }
    }

    fun delete(old: GeoFile) {
        viewModelScope.launch {
            geoFileRepository.delete(old).onLeft {
                Timber.e("failed to update geofile: ${it}")
            }
        }
    }

    fun download(gf: GeoFile) {
        viewModelScope.launch {
            geoFileRepository.download(gf).onLeft {
                Timber.e("failed to update geofile: ${it}")
            }
        }
    }
}
