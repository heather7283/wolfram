package io.github.heather7283.wolfram.data.xrayconfig

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configs")
data class XrayConfigEntity(
    @PrimaryKey
    val rowid: Long,

    val name: String,
    val text: String,
)
