package io.github.heather7283.wolfram.ui.settings

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.utils.CIDR
import io.github.heather7283.wolfram.utils.not
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections.emptyList
import javax.inject.Inject


sealed class SettingsPopup {
    data object Inactive : SettingsPopup()
    data class Cidr(
        val title: String,
        val ip: String?,
        val prefix: Int?,
        val onConfirm: (cidr: CIDR) -> Unit
    ) : SettingsPopup()
    data class Apps(
        val title: String,
        val onConfirm: (AppInfo) -> Unit
    ) : SettingsPopup()
}

data class SettingsUiState(
    val popup: SettingsPopup = SettingsPopup.Inactive,
    val statsEndpointModified: Boolean = false,
    val statsEndpointText: String = "",
    val statsPollIntervalModified: Boolean = false,
    val statsPollIntervalValue: Int = 0,
)

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var selected: Boolean,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings = settingsRepository.settingsFlow

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps = _apps.asStateFlow()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO: this is slow as balls
            settings.collect { settings ->
                _apps.value = app.packageManager.let { pm ->
                    val flags = PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
                    pm.getInstalledApplications(flags).filter {
                        !(it.flags and ApplicationInfo.FLAG_SYSTEM) // TODO: make this configurable?
                    }.map { app ->
                        AppInfo(
                            name = app.loadLabel(pm).toString(),
                            packageName = app.packageName,
                            icon = app.loadIcon(pm),
                            selected = settings.selectedApps.find { app.packageName == it } != null,
                        )
                    }
                }
            }
        }
    }

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
    fun openAppsPopup(
        title: String,
        onConfirm: (AppInfo) -> Unit,
    ) {
        _uiState.update {
            val popup = SettingsPopup.Apps(
                title = title,
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

    fun setSelectedAppsIsWhitelist(value: Boolean) = viewModelScope.launch {
        settingsRepository.setSelectedAppsIsWhitelist(value).onLeft { Timber.e(it) }
    }
    fun addSelectedApp(app: AppInfo) = viewModelScope.launch {
        settingsRepository.addSelectedApp(app.packageName).onLeft {
            Timber.e(it)
        }.onRight {
            closePopup()
        }
    }
    fun removeSelectedApp(app: AppInfo) = viewModelScope.launch {
        settingsRepository.removeSelectedApp(app.packageName).onLeft { Timber.e(it) }
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

    fun onStatsPollIntervalValueChange(value: Int) = _uiState.update {
        it.copy(statsPollIntervalModified = true, statsPollIntervalValue = value)
    }
    fun onStatsPollIntervalSave() = viewModelScope.launch {
        settingsRepository.setStatsPollInterval(_uiState.value.statsPollIntervalValue).onRight {
            _uiState.update {
                it.copy(statsPollIntervalModified = false)
            }
        }
    }
}
