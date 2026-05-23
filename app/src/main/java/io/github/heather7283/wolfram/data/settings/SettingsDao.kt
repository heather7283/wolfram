package io.github.heather7283.wolfram.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeAll(): Flow<SettingsEntity>

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getAll(): SettingsEntity

    @Query("SELECT vpnAddressList FROM settings WHERE id = 1")
    suspend fun getVpnAddressList(): String

    @Query("UPDATE settings SET vpnAddressList = :list WHERE id = 1")
    suspend fun updateVpnAddressList(list: String)

    @Insert
    suspend fun create(settings: SettingsEntity)
}
