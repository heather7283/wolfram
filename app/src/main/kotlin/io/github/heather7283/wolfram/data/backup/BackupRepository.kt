package io.github.heather7283.wolfram.data.backup

import android.app.Application
import android.net.Uri
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.pathString

@Singleton
class BackupRepository @Inject constructor(app: Application) {
    private val ctx = app.applicationContext
    private val db = WolframDatabase.getInstance(ctx)

    suspend fun backupDatabaseToUri(uri: Uri) = Either.catch {
        withContext(Dispatchers.IO) {
            val tmpfile = ctx.cacheDir.toPath().resolve("backup_temp_${System.currentTimeMillis()}")
            Files.deleteIfExists(tmpfile)

            db.query("VACUUM INTO '${tmpfile.pathString}';", emptyArray()).apply {
                moveToLast()
                close()
            }

            ctx.contentResolver.openOutputStream(uri)?.use { output ->
                tmpfile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Could not open stream for ${uri}")
        }
    }
}
