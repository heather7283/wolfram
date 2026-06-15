package io.github.heather7283.wolfram.data.settings

import io.github.heather7283.wolfram.utils.CIDR

data class Settings(
    val vpnAddresses: List<CIDR>,
    val vpnRoutes: List<CIDR>,

    val selectedApps: List<String>,
    val selectedAppsIsWhitelist: Boolean,

    val statsEnabled: Boolean,
    val statsEndpoint: String,
    val statsPollInterval: Int,

    val activeConfigId: Long,
)
