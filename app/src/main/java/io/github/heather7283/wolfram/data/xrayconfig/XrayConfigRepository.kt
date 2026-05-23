package io.github.heather7283.wolfram.data.xrayconfig

import android.app.Application
import arrow.core.Either
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Singleton
class XrayConfigRepository @Inject constructor(application: Application) {
    private val configFilesDir = Path(application.filesDir.path) / "configs"
    private val validNameRegex = Regex("""^[^|?*<>":+\[\]/\\]+$""")

    private val _configs = MutableStateFlow<List<XrayConfig>>(emptyList())

    init {
        configFilesDir.createDirectories()
        refreshConfigFiles()
    }

    private fun loadConfigFiles(): List<XrayConfig> {
        return configFilesDir.listDirectoryEntries()
            .filter { it.name.endsWith(".jsonc") }
            .map { XrayConfig(name = it.name.removeSuffix(".jsonc"), path = it) }
            .toList()
    }

    fun getConfigFiles(): List<XrayConfig> {
        return _configs.value
    }

    fun getConfigFilesFlow(): Flow<List<XrayConfig>> {
        return _configs.asStateFlow()
    }

    fun refreshConfigFiles() {
        _configs.update { loadConfigFiles() }
    }

    fun saveOrUpdateConfigFile(name: String, text: String) = Either.catch {
        require(validNameRegex.matchEntire(name) != null) { "Invalid config name" }

        val newConfig = XrayConfig(name, configFilesDir / "${name}.jsonc")
        newConfig.path.writeText(text)

        if (_configs.value.firstOrNull { it.name == newConfig.name } == null) {
            _configs.update { current -> listOf(newConfig) + current }
        }

        newConfig
    }

    fun deleteConfigFile(config: XrayConfig) = Either.catch {
        config.path.deleteExisting()
        _configs.update { current -> current.filter { it.name != config.name } }
    }

    fun getConfigFile(name: String) = Either.catch {
        val jsonPath = configFilesDir / "${name}.jsonc"
        check(jsonPath.exists()) { "Config $name does not exist" }
        XrayConfig(name, jsonPath)
    }

    fun getConfigFileText(config: XrayConfig) = Either.catch {
        config.path.readText()
    }
}
