package io.github.heather7283.wolfram.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileDao
import io.github.heather7283.wolfram.data.settings.Settings
import io.github.heather7283.wolfram.data.settings.SettingsDao
import io.github.heather7283.wolfram.data.settings.SettingsEntity
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigDao
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(entities=[GeoFile::class, SettingsEntity::class, XrayConfigEntity::class], version=1)
abstract class WolframDatabase : RoomDatabase() {
    abstract fun geoFileDao(): GeoFileDao
    abstract fun settingsDao(): SettingsDao
    abstract fun xrayConfigDao(): XrayConfigDao

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
                            activeConfigId = 0,
                        ))

                        val geoFiles = db.geoFileDao()
                        geoFiles.insert(GeoFile(
                            name = "geoip.dat",
                            url = "https://github.com/Loyalsoldier/v2ray-rules-dat/releases/latest/download/geoip.dat",
                        ))
                        geoFiles.insert(GeoFile(
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
                              "stats": {
                              },
                              "policy": {
                                "system": {
                                  "statsInboundUplink": true,
                                  "statsInboundDownlink": true,
                                  "statsOutboundUplink": true,
                                  "statsOutboundDownlink": true
                                }
                              },
                              "dns": {
                                "hosts": {
                                  "dns.google": [ "8.8.8.8", "8.8.4.4" ],
                                  "one.one.one.one": [ "1.1.1.1", "1.0.0.1" ],
                                  "dns.quad9.net": [ "9.9.9.9", "149.112.112.112" ]
                                },
                                "servers": [
                                  "https+local://dns.google/dns-query",
                                  "https+local://one.one.one.one/dns-query",
                                  "https+local://dns.quad9.net/dns-query"
                                ]
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
                                },
                                {
                                  "tag": "block",
                                  "protocol": "blackhole"
                                }
                              ],
                              "routing": {
                                "domainStrategy": "AsIs",
                                "rules": [
                                  {
                                    "ruleTag": "block-ads",
                                    "domain": [ "geosite:category-ads-all" ],
                                    "outboundTag": "block"
                                  }
                                ]
                              }
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
