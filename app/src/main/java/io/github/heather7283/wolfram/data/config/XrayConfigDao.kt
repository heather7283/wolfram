package io.github.heather7283.wolfram.data.config

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface XrayConfigDao {
    @Query("SELECT rowid AS id, name FROM configs")
    fun observeAll(): Flow<List<XrayConfigData>>

    @Query("SELECT rowid AS id, name FROM configs")
    fun getAll(): List<XrayConfigData>

    @Query("SELECT * FROM configs WHERE rowid = :id")
    suspend fun getById(id: Long): XrayConfigEntity

    @Query("SELECT text FROM configs WHERE rowid = :id")
    suspend fun getText(id: Long): String

    @Query("UPDATE configs SET name = :name, text = :text WHERE rowid = :id")
    suspend fun update(id: Long, name: String, text: String)

    @Query("INSERT INTO configs ( name, text ) VALUES ( :name, :text )")
    suspend fun insert(name: String, text: String): Long

    @Query("DELETE FROM configs WHERE rowid = :id")
    suspend fun delete(id: Long)
}
