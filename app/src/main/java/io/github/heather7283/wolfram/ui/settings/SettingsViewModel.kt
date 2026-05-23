package io.github.heather7283.wolfram.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileRepository
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings = settingsRepository.settingsFlow
}
