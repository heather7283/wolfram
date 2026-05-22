package io.github.heather7283.wolfram.ui.dashboard

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.WolframNavigationActions
import io.github.heather7283.wolfram.vpn.XrayInOutStat
import io.github.heather7283.wolfram.vpn.XrayStats
import java.text.DecimalFormat

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${DecimalFormat("0.#").format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${DecimalFormat("0.#").format(mb)} MB"
    return "${DecimalFormat("0.#").format(mb / 1024.0)} GB"
}

@Composable
private fun StatsInOut(stats: XrayInOutStat, modifier: Modifier = Modifier) {
    LazyColumn() {
        stats.forEach { (k, v) ->
            item {
                Text(k)
                Row() {
                    Column() {
                        Text("uplink")
                        Text(formatBytes(v.uplink))
                    }
                    Column() {
                        Text("downlink")
                        Text(formatBytes(v.downlink))
                    }
                }
            }
        }
    }
}

@Composable
private fun Stats(stats: XrayStats, modifier: Modifier = Modifier) {
    Text("Inbounds:")
    StatsInOut(stats.inbound)
    Spacer(Modifier.height(8.dp))
    Text("Outbounds:")
    StatsInOut(stats.outbound)
}

@Composable
fun DashboardScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val running = viewModel.running.collectAsStateWithLifecycle()
    val stats = viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val vpnPermsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpn()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (running.value) {
                        viewModel.stopVpn()
                    } else {
                        VpnService.prepare(context).also {
                            if (it != null) {
                                vpnPermsLauncher.launch(it)
                            } else {
                                viewModel.startVpn()
                            }
                        }
                    }
                },
                shape = CircleShape,
            ) {
                Icon(
                    if (running.value) { Icons.Default.Stop } else { Icons.Default.PlayArrow },
                    if (running.value) { "Stop" } else { "Start" } + " VPN"
                )
            }
        }
    ) { paddingValues ->
        Column() {
            Text(
                "VPN is " + if (running.value) { "running" } else { "not running" },
                modifier = Modifier.padding(paddingValues)
            )

            Spacer(Modifier.height(16.dp))

            stats.value.onLeft {
                Text("Could not fetch stats: ${it}", modifier = Modifier.fillMaxSize())
            }.onRight {
                Stats(it)
            }
        }
    }
}
