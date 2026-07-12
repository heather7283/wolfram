package io.github.heather7283.wolfram.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.heather7283.wolfram.data.settings.SettingsRepository
import io.github.heather7283.wolfram.data.xray.XrayRepository
import io.github.heather7283.wolfram.data.xray.XrayStatsOption
import io.github.heather7283.wolfram.utils.not
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

sealed class SortedXrayStats {
    data object Disabled : SortedXrayStats()
    data object Idle : SortedXrayStats()
    data class Error(val error: Throwable) : SortedXrayStats()
    // inbound/outbound, name/downlink/uplink
    typealias Stat = Pair<List<Triple<String, Long, Long>>, List<Triple<String, Long, Long>>>
    data class Stats(val stats: Stat) : SortedXrayStats()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val xrayRepository: XrayRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val running = xrayRepository.running

    val stats = combine(settingsRepository.settingsFlow, xrayRepository.stats) { settings, stats ->
        if (!settings.statsEnabled) {
            SortedXrayStats.Disabled
        } else when (stats) {
            is XrayStatsOption.Idle -> SortedXrayStats.Idle
            is XrayStatsOption.Error -> SortedXrayStats.Error(stats.err)
            is XrayStatsOption.Success -> stats.stats.let {
                // TODO: is it possible to remove duplicate code?
                val inbounds = it.inbound
                    .map { (k, v) -> Triple(k, v.downlink, v.uplink) }
                    .filterNot { (_, downlink, uplink) -> !downlink && !uplink }
                    .sortedWith(compareBy({ it.second }, { it.third }, { it.first }))
                    .asReversed()
                val outbounds = it.outbound
                    .map { (k, v) -> Triple(k, v.downlink, v.uplink) }
                    .filterNot { (_, downlink, uplink) -> !downlink && !uplink }
                    .sortedWith(compareBy({ it.second }, { it.third }, { it.first }))
                    .asReversed()
                SortedXrayStats.Stats(Pair(inbounds, outbounds))
            }
        }
    }

    fun startVpn() = xrayRepository.startVpn()
    fun stopVpn() = xrayRepository.stopVpn()
}
