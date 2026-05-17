package io.github.heather7283.wolfram.data.geofile

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName="geofiles")
data class GeoFile(
    @PrimaryKey val name: String,
    val url: String,
    val existsLocally: Boolean,
    val lastUpdated: Date?,
    val size: Long?,
)
