package io.github.heather7283.wolfram.data.geofile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoFileDao {
    @Query("SELECT * FROM geofiles")
    fun observeAll(): Flow<List<GeoFile>>

    @Query("SELECT * FROM geofiles WHERE name = :name")
    suspend fun get(name: String): GeoFile?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(geoFile: GeoFile)

    @Update
    suspend fun update(geoFile: GeoFile): Int

    @Delete
    suspend fun delete(geoFile: GeoFile): Int
}
