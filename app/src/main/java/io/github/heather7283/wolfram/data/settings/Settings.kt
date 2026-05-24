package io.github.heather7283.wolfram.data.settings

import io.github.heather7283.wolfram.utils.CIDR

data class Settings(
    val vpnAddresses: List<CIDR>,
    val vpnRoutes: List<CIDR>,

    val activeConfigId: Long,
)
