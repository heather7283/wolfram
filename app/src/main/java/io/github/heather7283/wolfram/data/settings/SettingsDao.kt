package io.github.heather7283.wolfram.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert
    suspend fun create(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeAll(): Flow<SettingsEntity>

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getAll(): SettingsEntity

    @Query("SELECT vpnAddressList FROM settings WHERE id = 1")
    suspend fun getVpnAddressList(): String

    @Query("UPDATE settings SET vpnAddressList = :list WHERE id = 1")
    suspend fun updateVpnAddressList(list: String)

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
}
