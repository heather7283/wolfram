package io.github.heather7283.wolfram.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.geofile.GeoFileDao

@Database(entities=[GeoFile::class], version=1)
abstract class WolframDatabase : RoomDatabase() {
    abstract fun geoFileDao(): GeoFileDao

    companion object {
        private var instance: WolframDatabase? = null
        fun getInstance(ctx: Context) = instance ?: Room.databaseBuilder(
            ctx, WolframDatabase::class.java, "wolfram"
        ).build().also { instance = it }
    }
}
