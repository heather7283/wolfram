package io.github.heather7283.wolfram.data

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
class ConfigsRepository @Inject constructor(application: Application) {
    private val configsDir = Path(application.filesDir.path) / "configs"
    private val validNameRegex = Regex("""^[^|?*<>":+\[\]/\\]+$""")

    private val _configs = MutableStateFlow<List<XrayConfig>>(emptyList())

    init {
        configsDir.createDirectories()
        refreshConfigs()
    }

    private fun loadConfigs(): List<XrayConfig> {
        return configsDir.listDirectoryEntries()
            .filter { it.name.endsWith(".jsonc") }
            .map { XrayConfig(name = it.name.removeSuffix(".jsonc"), jsonPath = it) }
            .toList()
    }

    fun getConfigsFlow(): Flow<List<XrayConfig>> {
        return _configs.asStateFlow()
    }

    fun refreshConfigs() {
        _configs.update { loadConfigs() }
    }

    fun saveConfig(name: String, text: String) = Either.catch {
        require(validNameRegex.matchEntire(name) != null) { "Invalid config name" }

        val newConfig = XrayConfig(name, configsDir / "${name}.jsonc")
        newConfig.jsonPath.writeText(text)

        if (_configs.value.firstOrNull { it.name == newConfig.name } == null) {
            _configs.update { current -> listOf(newConfig) + current }
        }

        newConfig
    }

    fun deleteConfig(config: XrayConfig) = Either.catch {
        config.jsonPath.deleteExisting()
        _configs.update { current -> current.filter { it.name != config.name } }
    }

    fun getConfig(name: String) = Either.catch {
        val jsonPath = configsDir / "${name}.jsonc"
        check(jsonPath.exists()) { "Config $name does not exist" }
        XrayConfig(name, jsonPath)
    }

    fun getConfigText(config: XrayConfig) = Either.catch {
        config.jsonPath.readText()
    }
}