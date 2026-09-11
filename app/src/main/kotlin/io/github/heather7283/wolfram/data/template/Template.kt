package io.github.heather7283.wolfram.data.template

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "templates",
    indices = [ Index(value = [ "key" ], unique = true) ],
)
data class Template(
    @PrimaryKey
    val id: Int,

    val key: String,
    val replacement: String,
)
