package io.github.heather7283.wolfram.data.template

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates")
    fun observeAll(): Flow<List<Template>>

    @Query("INSERT OR REPLACE INTO templates ( id, key, replacement ) VALUES ( :id, :key, :replacement )")
    suspend fun upsert(id: Int?, key: String, replacement: String)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: Int)
}
