package io.github.heather7283.wolfram.data

import android.content.Context
import androidx.core.content.edit
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import io.github.heather7283.wolfram.data.config.XrayConfigDao
import io.github.heather7283.wolfram.data.config.XrayConfigEntity
import io.github.heather7283.wolfram.data.geofile.GeoFileEntity
import io.github.heather7283.wolfram.data.geofile.GeoFileDao
import io.github.heather7283.wolfram.data.settings.SettingsDao
import io.github.heather7283.wolfram.data.settings.SettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Database(
    version = 2,
    exportSchema = true,
    entities = [
        GeoFileEntity::class,
        SettingsEntity::class,
        XrayConfigEntity::class
   ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WolframDatabase.AutoMigrationFrom1To2::class),
    ]
)
abstract class WolframDatabase : RoomDatabase() {
    abstract fun geoFileDao(): GeoFileDao
    abstract fun settingsDao(): SettingsDao
    abstract fun xrayConfigDao(): XrayConfigDao

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "geofiles", columnName = "existsLocally"),
        DeleteColumn(tableName = "geofiles", columnName = "lastUpdated"),
        DeleteColumn(tableName = "geofiles", columnName = "size"),
    )
    class AutoMigrationFrom1To2 : AutoMigrationSpec

    companion object {
        val callback = object : Callback() {
            override fun onCreate(connection: SQLiteConnection) {
                super.onCreate(connection)

                instance?.let { db ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val settings = db.settingsDao()
                        settings.create(SettingsEntity(
                            vpnAddressList = """[ "10.20.30.1/24" ]""",
                            vpnRouteList = """[ "0.0.0.0/0" ]""",
                            selectedAppsList = """[]""",
                            selectedAppsIsWhitelist = false,
                            activeConfigId = 0,
                            statsEnabled = true,
                            statsEndpoint = "127.0.0.1:54321",
                            statsPollInterval = 5,
                        ))

                        val geoFiles = db.geoFileDao()
                        geoFiles.insert(GeoFileEntity(
                            name = "geoip.dat",
                            url = "https://github.com/Loyalsoldier/v2ray-rules-dat/releases/latest/download/geoip.dat",
                        ))
                        geoFiles.insert(GeoFileEntity(
                            name = "geosite.dat",
                            url = "https://github.com/Loyalsoldier/v2ray-rules-dat/releases/latest/download/geosite.dat",
                        ))

                        val configs = db.xrayConfigDao()
                        configs.insert("default", """
                            {
                              "log": {
                                "loglevel": "error"
                              },
                              "metrics": {
                                "tag": "metrics",
                                "listen": "127.0.0.1:54321"
                              },
                              "stats": {},
                              "policy": {
                                "system": {
                                  "statsInboundUplink": true,
                                  "statsInboundDownlink": true,
                                  "statsOutboundUplink": true,
                                  "statsOutboundDownlink": true
                                }
                              },
                              "inbounds": [
                                {
                                  "tag": "tun-in",
                                  "protocol": "tun",
                                  "settings": {
                                    "name": "xray0",
                                    "gateway": [ "10.20.30.1" ]
                                  },
                                  "sniffing": {
                                    "enabled": true,
                                    "destOverride": [ "http", "tls", "quic" ],
                                    "routeOnly": true
                                  }
                                }
                              ],
                              "outbounds": [
                                {
                                  "tag": "direct-out",
                                  "protocol": "freedom"
                                }
                              ]
                            }
                        """.trimIndent())
                    }
                }
            }
        }

        // thank you so much kind sir at https://blog.termian.dev/posts/room-on-upgrade/
        class CorruptionCallback(
            private val delegate: SupportSQLiteOpenHelper.Callback,
        ) : SupportSQLiteOpenHelper.Callback(delegate.version) {

            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                delegate.onDowngrade(db, oldVersion, newVersion)
            }

            override fun onCreate(db: SupportSQLiteDatabase) {
                delegate.onCreate(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                delegate.onOpen(db)
            }

            override fun onConfigure(db: SupportSQLiteDatabase) {
                delegate.onConfigure(db)
            }

            override fun onCorruption(db: SupportSQLiteDatabase) {
                throw Exception("Database is corrupted")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                delegate.onUpgrade(db, oldVersion, newVersion)
            }
        }
        class CorruptionOpenHelperFactory(
            private val delegate: SupportSQLiteOpenHelper.Factory,
        ) : SupportSQLiteOpenHelper.Factory {

            override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
                val decoratedConfiguration =
                    SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                        .name(configuration.name)
                        .callback(CorruptionCallback(configuration.callback))
                        .build()
                return delegate.create(decoratedConfiguration)
            }
        }

        private val dbName = "wolfram"
        private var instance: WolframDatabase? = null

        private fun deleteDatabaseFiles(ctx: Context) {
            // I hate everything about this API https://stackoverflow.com/a/61530578
            Files.deleteIfExists(ctx.getDatabasePath(dbName).toPath())
            Files.deleteIfExists(File(ctx.getDatabasePath(dbName).absolutePath + "-wal").toPath())
            Files.deleteIfExists(File(ctx.getDatabasePath(dbName).absolutePath + "-shm").toPath())
        }

        fun getInstance(ctx: Context): WolframDatabase {
            instance?.let { return it }

            Timber.d("getInstance called for the first time")

            val prefs = ctx.getSharedPreferences("restore", Context.MODE_PRIVATE)
            val needsRestore = prefs.getBoolean("needs_restore", false)

            Timber.d("getInstance: needs_restore=${needsRestore}")

            if (!needsRestore) {
                return Room.databaseBuilder(ctx, WolframDatabase::class.java, dbName)
                    .addCallback(callback)
                    .build()
                    .also { instance = it }
            }

            // if we got here, needs_restore is 1
            Timber.d("needs_restore=1, opening newdb")
            prefs.edit {
                putBoolean("needs_restore", false)
                commit()
            }

            // TODO: clean up those files
            val newdb = ctx.dataDir.toPath().resolve("newdb")
            val olddb = ctx.dataDir.toPath().resolve("olddb")

            try {
                deleteDatabaseFiles(ctx)
                val db = Room.databaseBuilder(ctx, WolframDatabase::class.java, dbName)
                    .addCallback(callback)
                    .openHelperFactory(CorruptionOpenHelperFactory(
                        FrameworkSQLiteOpenHelperFactory()
                    ))
                    .createFromFile(newdb.toFile())
                    .build()
                    .also { instance = it }
                db.openHelper.writableDatabase // touch
                return db
            } catch (e: Exception) {
                Timber.e(e, "failed to load newdb, setting restore_failed=1")
                prefs.edit {
                    putBoolean("restore_failed", true)
                    putString("restore_failed_reason", e.message ?: "Unknown error")
                    commit()
                }
            }

            // if we got here, restoring the db failed. Nuke the files and try again with olddb
            deleteDatabaseFiles(ctx)
            return Room.databaseBuilder(ctx, WolframDatabase::class.java, dbName)
                .addCallback(callback)
                .createFromFile(olddb.toFile())
                .build()
                .also { instance = it }
        }
    }
}
