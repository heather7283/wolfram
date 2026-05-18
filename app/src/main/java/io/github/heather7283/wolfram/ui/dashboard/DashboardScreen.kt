package io.github.heather7283.wolfram.ui.dashboard

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
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
        Text(
            "VPN is " + if (running.value) { "running" } else { "not running" },
            modifier = Modifier.padding(paddingValues)
        )
    }
}
