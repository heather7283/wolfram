package io.github.heather7283.wolfram.data

import android.app.Application
import jakarta.inject.Inject
import jakarta.inject.Singleton
import timber.log.Timber
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Singleton
class ConfigsRepository @Inject constructor(application: Application) {
    private val configsDir = Path(application.filesDir.path) / "configs"
    private val badCharRegex = Regex("""[|?*<>":+\[\]/\\]""")

    init {
        try {
            configsDir.createDirectories()
        } catch (_: FileAlreadyExistsException) {
            Timber.w("configsDir already exists and is not a dir, attempting to delete and recreate")
            configsDir.deleteIfExists()
            configsDir.createDirectory()
        }

        for (i in 1..30) {
            addOrUpdateConfig("test${i}", "{}")
        }
    }

    fun getConfigs(): List<XrayConfig> {
        return configsDir.listDirectoryEntries()
            .map { XrayConfig(name = it.name.removeSuffix(".jsonc"), jsonPath = it) }
            .toList()
    }

    // TODO: proper error reporting
    fun addOrUpdateConfig(name: String, text: String): String? {
        if (badCharRegex.containsMatchIn(name)) {
            return "Invalid character(s) in config name"
        }

        val configFile = configsDir / name
        if (configFile.runCatching { writeText(text) }.isFailure) {
            return "Failed to write config content to ${configFile}"
        }

        return null
    }

    fun getConfigText(name: String): String {
        val configFile = configsDir / "${name}.jsonc"
        return configFile.readText()
    }
}