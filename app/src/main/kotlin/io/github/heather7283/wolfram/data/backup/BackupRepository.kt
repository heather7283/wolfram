package io.github.heather7283.wolfram.data.backup

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import androidx.core.content.edit
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

@Singleton
class BackupRepository @Inject constructor(app: Application) {
    private val ctx = app.applicationContext
    private val db = WolframDatabase.getInstance(ctx)

    private fun vacuumInto(path: Path) = db.query("VACUUM INTO '${path.pathString}';", emptyArray())
        .apply { moveToLast() }
        .close()

    suspend fun backupDatabaseToUri(uri: Uri) = Either.catch {
        withContext(Dispatchers.IO) {
            val tmpfile = ctx.cacheDir.toPath().resolve("backup_temp")
            Files.deleteIfExists(tmpfile)

            vacuumInto(tmpfile)

            ctx.contentResolver.openOutputStream(uri)?.use { output ->
                tmpfile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Could not open stream for ${uri}")
        }
    }

    suspend fun restoreDatabaseFromUri(uri: Uri) = Either.catch {
        withContext(Dispatchers.IO) {
            val prefs = ctx.getSharedPreferences("restore", MODE_PRIVATE)

            val needsRestore = prefs.getBoolean("needs_restore", false)
            check(!needsRestore) { "Settings restore already triggered, restart the app" }

            val olddb = ctx.dataDir.toPath().resolve("olddb")
            val newdb = ctx.dataDir.toPath().resolve("newdb")
            Files.deleteIfExists(olddb)

            vacuumInto(olddb)
            check(Files.size(olddb) > 0) { "Empty olddb (vacuum failed?)" }

            ctx.contentResolver.openInputStream(uri)?.use { input ->
                newdb.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Could not open stream for ${uri}")

            check(Files.size(newdb) > 0) { "Empty newdb" }

            Timber.d("Settings needs_restore=1")
            prefs.edit {
                putBoolean("needs_restore", true)
                commit()
            }
        }
    }
}
