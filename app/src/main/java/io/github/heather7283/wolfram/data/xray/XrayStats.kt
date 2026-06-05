package io.github.heather7283.wolfram.data.xray

import arrow.core.Either
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class XrayUpDownStat(
    val uplink: Long,
    val downlink: Long,
)

typealias XrayInOutStat = Map<String, XrayUpDownStat>

@Serializable
data class XrayStats(
    val inbound: XrayInOutStat = emptyMap(),
    val outbound: XrayInOutStat = emptyMap(),
)

@Serializable
private data class XrayResponse(
    val stats: XrayStats,
)

fun parseXrayStats(jsonString: String) = Either.catch {
    val json = Json {
        ignoreUnknownKeys = true
    }

    json.decodeFromString<XrayResponse>(jsonString).stats
}
