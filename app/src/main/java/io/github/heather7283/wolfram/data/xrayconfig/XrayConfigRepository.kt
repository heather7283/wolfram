package io.github.heather7283.wolfram.data.xrayconfig

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
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
class XrayConfigRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).xrayConfigDao()

    val configsFlow = dao.observeAll()

    fun getConfigs(): List<XrayConfigData> = dao.getAll()

    suspend fun create(name: String, text: String) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.insert(name, text)
        }
    }

    suspend fun update(id: Long, name: String, text: String) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.update(id, name, text)
        }
    }

    suspend fun delete(id: Long) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.delete(id)
        }
    }

    suspend fun getById(id: Long) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }
}
