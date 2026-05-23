package io.github.heather7283.wolfram.data.geofile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="geofiles")
data class GeoFile(
    @PrimaryKey val name: String,
    val url: String,
    val existsLocally: Boolean = false,
    // I couldn't get TypeConverter nonsense to work
    // so I'm just gonna use Long instead of Date here
    val lastUpdated: Long? = null,
    val size: Long? = null,
)
