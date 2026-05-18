package io.github.heather7283.wolfram.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileDao
import java.sql.Date

@Database(entities=[GeoFile::class], version=1, exportSchema=false)
abstract class WolframDatabase : RoomDatabase() {
    abstract fun geoFileDao(): GeoFileDao

    companion object {
        private var instance: WolframDatabase? = null
        fun getInstance(ctx: Context) = instance ?: Room.databaseBuilder(
            ctx, WolframDatabase::class.java, "wolfram"
        ).build().also { instance = it }
    }
}
