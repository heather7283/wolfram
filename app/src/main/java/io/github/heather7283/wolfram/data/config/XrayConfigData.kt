package io.github.heather7283.wolfram.data.config

// separate from the entity to not pull in large config text by default
data class XrayConfigData(
    val id: Long,
    val name: String,
)
