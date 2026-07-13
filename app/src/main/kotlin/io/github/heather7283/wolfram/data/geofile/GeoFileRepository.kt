package io.github.heather7283.wolfram.data.geofile

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.use
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.outputStream

@Singleton
class GeoFileRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).geoFileDao()
    private val http = OkHttpClient()

    val geoFilesDir: Path = app.filesDir.toPath().resolve("geofiles").also(Files::createDirectories)
    private fun pathFor(name: String) = geoFilesDir.resolve(name)
    private fun GeoFileEntity.path() = pathFor(this.name)
    private fun GeoFile.path() = pathFor(this.name)

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    fun refreshGeoFiles() = refreshTrigger.tryEmit(Unit)

    val geoFiles = combine(dao.observeAll(), refreshTrigger) { entities, _ ->
        entities
    }.map { entities ->
        entities.map { entity ->
            val path = entity.path()
            val exists = path.exists()
            val size = if (!exists) null else {
                path.fileSize()
            }
            val mtime = if (!exists) null else {
                path.getLastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
            }
            GeoFile(
                name = entity.name,
                url = entity.url,
                existsLocally = exists,
                size = size,
                mtime = mtime,
            )
        }
    }

    suspend fun add(name: String, url: String) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.insert(GeoFileEntity(name = name, url = url))
        }
    }

    suspend fun download(name: String) = Either.catch {
        withContext(Dispatchers.IO) {
            val gf = checkNotNull(dao.get(name))

            val targetFile = gf.path()
            val tempFile = pathFor("${gf.name}.tmp")

            try {
                http.newCall(Request.Builder().url(gf.url).build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Got HTTP ${response.code} for ${gf.url}")
                    }
                    val body = response.body
                    tempFile.outputStream().use { body.byteStream().copyTo(it) }
                }
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Files.deleteIfExists(tempFile)
                throw e
            }

            refreshGeoFiles()
        }
    }

    suspend fun modify(oldName: String, name: String, url: String) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = checkNotNull(dao.get(oldName))

            if (old.name != name) {
                require (dao.get(name) == null) { "geofile ${name} already exists" }
                if (old.path().exists()) {
                    Files.move(old.path(), pathFor(name))
                }
                dao.delete(oldName)
                dao.insert(GeoFileEntity(name, url))
            }

            if (old.url != url) {
                // changed url so need redownload
                Files.deleteIfExists(pathFor(name))
                refreshGeoFiles()
            }
        }
    }

    suspend fun delete(old: String) = Either.catch {
        withContext(Dispatchers.IO) {
            Files.deleteIfExists(pathFor(old))
            dao.delete(old)
        }
    }
}
