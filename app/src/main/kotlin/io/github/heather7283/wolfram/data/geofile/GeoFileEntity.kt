package io.github.heather7283.wolfram.data.geofile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="geofiles")
data class GeoFileEntity(
    @PrimaryKey val name: String,
    val url: String,
)
