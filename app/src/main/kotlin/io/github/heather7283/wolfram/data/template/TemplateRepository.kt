package io.github.heather7283.wolfram.data.template

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TemplateRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).templateDao()

    val templatesFlow = dao.observeAll()

    suspend fun getAll() = Either.catch {
        dao.getAll()
    }

    suspend fun upsert(id: Int? = null, key: String, replacement: String) = Either.catch {
        require(!key.startsWith("WOLFRAM_")) {
            "Template keys starting with WOLFRAM_ are reserved"
        }
        require(!key.contains('@')) {
            "Template key must not contain @ character"
        }

        withContext(Dispatchers.IO) {
            dao.upsert(id, key, replacement)
        }
    }

    suspend fun delete(id: Int) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.delete(id)
        }
    }
}
