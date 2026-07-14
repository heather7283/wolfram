package io.github.heather7283.wolfram.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import io.github.heather7283.wolfram.data.config.XrayConfigDao
import io.github.heather7283.wolfram.data.config.XrayConfigEntity
import io.github.heather7283.wolfram.data.geofile.GeoFileEntity
import io.github.heather7283.wolfram.data.geofile.GeoFileDao
import io.github.heather7283.wolfram.data.settings.SettingsDao
import io.github.heather7283.wolfram.data.settings.SettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        private var instance: WolframDatabase? = null
        fun getInstance(ctx: Context) = instance ?: Room.databaseBuilder(
            ctx, WolframDatabase::class.java, "wolfram"
        ).addCallback(callback).build().also { instance = it }
    }
}
