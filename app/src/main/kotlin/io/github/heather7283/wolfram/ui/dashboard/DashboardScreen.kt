package io.github.heather7283.wolfram.ui.dashboard

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.ui.WolframNavigationActions
import io.github.heather7283.wolfram.utils.formatBytes

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.border(Dp.Hairline, Color.Gray).weight(weight).padding(8.dp),
        maxLines = 1,
        overflow = TextOverflow.MiddleEllipsis,
    )
}

@Composable
private fun Table(
    rows: Int,
    columns: Int,
    headers: List<String>,
    weights: List<Float>,
    items: (row: Int, col: Int) -> String,
) {
    LazyColumn {
        stickyHeader {
            Row(modifier = Modifier.fillMaxWidth()) {
                headers.forEachIndexed { column, header ->
                    TableCell(header, weight = weights[column], isHeader = true)
                }
            }
        }
        items(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (column in 0..<columns) {
                    TableCell(items(row, column), weight = weights[column])
                }
            }
        }
    }
}

@Composable
private fun CoreStats(stats: SortedXrayStats, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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

                    @Composable fun showTable(
                        direction: String,
                        stats: List<Triple<String, Long, Long>>,
                    ) = Table(
                        rows = stats.size,
                        columns = 3,
                        weights = listOf(1.5f, 1f, 1f),
                        headers = listOf(direction, "Downlink", "Uplink"),
                    ) { row, col ->
                        when (col) {
                            0 -> stats[row].first
                            1 -> formatBytes(stats[row].second)
                            else -> formatBytes(stats[row].third)
                        }
                    }

                    if (!inbound.isEmpty()) {
                        showTable("Inbound", inbound)
                    }
                    if (!outbound.isEmpty()) {
                        showTable("Outbound", outbound)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreStatus(running: Boolean, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Status", style = MaterialTheme.typography.headlineSmall)
            Text("Core is " + if (running) { "running" } else { "not running" })
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
            CoreStatus(running.value)
            CoreStats(stats.value)
        }
    }
}
