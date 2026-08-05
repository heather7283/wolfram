package io.github.heather7283.wolfram.data.template

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey
    val id: Int,

    val key: String,
    val replacement: String,
)
