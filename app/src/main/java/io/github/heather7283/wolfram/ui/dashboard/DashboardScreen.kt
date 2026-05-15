package io.github.heather7283.wolfram.ui.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.WolframNavigationActions

@Composable
fun DashboardScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val running = viewModel.running.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val logLen = viewModel.log.size
    LaunchedEffect(logLen) {
        if (logLen > 0) {
            listState.scrollToItem(logLen - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
        floatingActionButton = {
            FloatingActionButton(
                { if (running.value) { viewModel.stopVpn() } else { viewModel.startVpn() } },
                shape = CircleShape,
            ) {
                Icon(
                    if (running.value) { Icons.Default.Stop } else { Icons.Default.PlayArrow },
                    if (running.value) { "Stop" } else { "Start" } + " VPN"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(state = listState, modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            items(viewModel.log) { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}
