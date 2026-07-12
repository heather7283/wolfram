package io.github.heather7283.wolfram.ui.dashboard

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunnychung.lib.android.composabletable.ux.Table
import io.github.heather7283.wolfram.ui.WolframNavigationActions
import io.github.heather7283.wolfram.utils.formatBytes
import io.github.heather7283.wolfram.utils.not

@Composable
private fun Cell(
    text: String,
) {
    Box(modifier = Modifier.border(width = 1.dp, color = Color.Gray)) {
        Text(text = text, modifier = Modifier.padding(4.dp))
    }
}

@Composable
private fun StatsInOut(
    label: String,
    stats: List<Triple<String, Long, Long>>,
    modifier: Modifier = Modifier
) {
    Table(
        rowCount = stats.size + 1,
        columnCount = 3,
        modifier = modifier.fillMaxWidth(),
    ) { row, column ->
        when (column) {
            0 -> Cell(if (!row) { label } else { stats[row - 1].first })
            1 -> Cell(if (!row) { "downlink" } else { formatBytes(stats[row - 1].second) })
            2 -> Cell(if (!row) { "uplink" } else { formatBytes(stats[row - 1].third) })
        }
    }
}

@Composable
private fun Stats(stats: SortedXrayStats, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Traffic", style = MaterialTheme.typography.headlineSmall)
            when (stats) {
                is SortedXrayStats.Disabled -> Text("Stats collection is disabled in settings")
                is SortedXrayStats.Idle -> Text("Core is not running")
                is SortedXrayStats.Error -> Text("Could not collect stats: ${stats.error}")
                is SortedXrayStats.Stats -> stats.stats.also { (inbound, outbound) ->
                    if (inbound.isEmpty() && outbound.isEmpty()) {
                        Text("Nothing to show")
                        return@also
                    }

                    if (!inbound.isEmpty()) {
                        Column() {
                            Text("Inbounds")
                            StatsInOut("Inbound", inbound)
                        }
                    }
                    if (!outbound.isEmpty()) {
                        Column() {
                            Text("Outbounds")
                            StatsInOut("Outbound", outbound)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val running = viewModel.running.collectAsStateWithLifecycle()
    val stats = viewModel.stats.collectAsStateWithLifecycle(SortedXrayStats.Idle)
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
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Status", style = MaterialTheme.typography.headlineSmall)
                    Text("Core is " + if (running.value) { "running" } else { "not running" })
                }
            }
            Stats(stats.value)
        }
    }
}
