package io.github.heather7283.wolfram.data.settings

import io.github.heather7283.wolfram.utils.CIDR
import java.net.InetAddress

data class Settings(
    val vpnAddresses: List<CIDR>,
    val vpnRoutes: List<CIDR>,
    val dnsAddresses: List<InetAddress>,

    val selectedApps: List<String>,
    val selectedAppsIsWhitelist: Boolean,

    val statsEnabled: Boolean,
    val statsEndpoint: String,
    val statsPollInterval: Int,

    val activeConfigId: Long,
)
