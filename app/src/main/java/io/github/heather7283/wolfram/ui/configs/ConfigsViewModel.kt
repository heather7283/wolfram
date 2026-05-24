package io.github.heather7283.wolfram.ui.configs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigData
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfigsViewModel @Inject constructor(
    private val xrayConfigRepository: XrayConfigRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val configs = xrayConfigRepository.configsFlow
    val activeConfig = settingsRepository.settingsFlow.map { it.activeConfigId }

    fun delete(id: Long) {
        viewModelScope.launch { xrayConfigRepository.delete(id) }
    }

    fun setActive(id: Long) {
        viewModelScope.launch { settingsRepository.setActiveConfigId(id) }
    }
}
