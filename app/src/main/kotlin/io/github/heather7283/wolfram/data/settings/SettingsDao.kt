package io.github.heather7283.wolfram.data.settings

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("INSERT OR IGNORE INTO settings DEFAULT VALUES")
    suspend fun seed()

    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeAll(): Flow<SettingsEntity>

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getAll(): SettingsEntity

    @Query("SELECT vpnAddressList FROM settings WHERE id = 1")
    suspend fun getVpnAddressList(): String
    @Query("UPDATE settings SET vpnAddressList = :list WHERE id = 1")
    suspend fun setVpnAddressList(list: String)

    @Query("SELECT vpnRouteList FROM settings WHERE id = 1")
    suspend fun getVpnRouteList(): String
    @Query("UPDATE settings SET vpnRouteList = :list WHERE id = 1")
    suspend fun setVpnRouteList(list: String)

    @Query("SELECT vpnIsMetered FROM settings WHERE id = 1")
    suspend fun getVpnIsMetered(): Boolean
    @Query("UPDATE settings SET vpnIsMetered = :isMetered WHERE id = 1")
    suspend fun setVpnIsMetered(isMetered: Boolean)

    @Query("SELECT dnsAddressList FROM settings WHERE id = 1")
    suspend fun getDnsAddressList(): String
    @Query("UPDATE settings SET dnsAddressList = :list WHERE id = 1")
    suspend fun setDnsAddressList(list: String)

    @Query("SELECT selectedAppsList FROM settings WHERE id = 1")
    suspend fun getSelectedAppsList(): String
    @Query("UPDATE settings SET selectedAppsList = :list WHERE id = 1")
    suspend fun setSelectedAppsList(list: String)

    @Query("UPDATE settings SET selectedAppsIsWhitelist = :isWhitelist WHERE id = 1")
    suspend fun setSelectedAppsIsWhitelist(isWhitelist: Boolean)

    @Query("SELECT activeConfigId FROM settings WHERE id = 1")
    suspend fun getActiveConfigId(): Long
    @Query("UPDATE settings SET activeConfigId = :id WHERE id = 1")
    suspend fun setActiveConfigId(id: Long)

    @Query("UPDATE settings SET statsEnabled = :enabled WHERE id = 1")
    suspend fun setStatsEnabled(enabled: Boolean)
    @Query("UPDATE settings SET statsEndpoint = :endpoint WHERE id = 1")
    suspend fun setStatsEndpoint(endpoint: String)
    @Query("UPDATE settings SET statsPollInterval = :seconds WHERE id = 1")
    suspend fun setStatsPollInterval(seconds: Int)
}
