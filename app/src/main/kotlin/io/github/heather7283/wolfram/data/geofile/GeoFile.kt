package io.github.heather7283.wolfram.data.geofile

import java.time.LocalDateTime

data class GeoFile(
    val name: String,
    val url: String,
    val existsLocally: Boolean,
    val size: Long?,
    val mtime: LocalDateTime?,
)
