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

    @Query("SELECT * FROM templates")
    suspend fun getAll(): List<Template>

    @Query("""
        INSERT INTO templates ( id, key, replacement ) VALUES ( :id, :key, :replacement )
        ON CONFLICT ( id ) DO UPDATE SET key=excluded.key, replacement=excluded.replacement
    """)
    suspend fun upsert(id: Int?, key: String, replacement: String)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: Int)
}
