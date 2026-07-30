package io.github.heather7283.wolfram.data.config

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getNameById(id: Long) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.getNameById(id) ?: throw NoSuchElementException(
                "Config with id ${id} does not exist"
            )
        }
    }
}
