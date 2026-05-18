package io.github.heather7283.wolfram.data.geofile

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.use
import java.nio.file.Files
import java.nio.file.Path
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

@Singleton
class GeoFileRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).geoFileDao()
    private val http = OkHttpClient()

    val geoFilesDir: Path = app.filesDir.toPath().resolve("geofiles").also {
        Files.createDirectories(it)
    }
    private fun pathFor(name: String) = geoFilesDir.resolve(name)
    private fun GeoFile.path() = pathFor(this.name)

    val geoFiles = dao.observeAll()

    suspend fun add(name: String, url: String) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.insert(GeoFile(
                name = name,
                url = url,
                existsLocally = false,
                lastUpdated = null,
                size = null,
            ))
        }
    }

    suspend fun download(gf: GeoFile) = Either.catch {
        withContext(Dispatchers.IO) {
            val targetFile = gf.path()
            val tempFile = pathFor("${gf.name}.tmp")

            try {
                http.newCall(Request.Builder().url(gf.url).build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Got HTTP ${response.code} for ${gf.url}")
                    }
                    val body = response.body ?: throw IOException("Empty response for ${gf.url}")
                    tempFile.outputStream().use { body.byteStream().copyTo(it) }
                }
                Files.move(tempFile, targetFile)
            } catch (e: Exception) {
                Files.deleteIfExists(tempFile)
                throw e
            }

            dao.update(gf.copy(
                existsLocally = true,
                lastUpdated = Date().time,
                size = targetFile.fileSize(),
            ))
        }
    }

    suspend fun modify(old: GeoFile, name: String, url: String) = Either.catch {
        withContext(Dispatchers.IO) {
            if (old.name != name) {
                require (dao.get(name) == null) { "geofile ${name} already exists" }
                if (old.existsLocally) {
                    Files.move(old.path(), pathFor(name))
                }
                dao.delete(old)
                dao.insert(old.copy(name = name))
            }

            if (old.url != url) {
                // changed url so need redownload
                Files.deleteIfExists(pathFor(name))
                dao.update(old.copy(
                    name = name,
                    url = url,
                    existsLocally = false,
                    lastUpdated = null,
                    size = null,
                ))
            }
        }
    }

    suspend fun delete(old: GeoFile) = Either.catch {
        withContext(Dispatchers.IO) {
            Files.deleteIfExists(old.path())
            dao.delete(old)
        }
    }
}
