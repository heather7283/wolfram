package io.github.heather7283.wolfram.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.WolframNavigationActions
import io.github.heather7283.wolfram.data.geofile.GeoFile
import io.github.heather7283.wolfram.data.settings.Settings
import io.github.heather7283.wolfram.utils.CIDR
import timber.log.Timber
import java.util.Date

@Composable
private fun CidrCard(
    cidr: CIDR,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${cidr.ip.hostAddress}/${cidr.prefix}")
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun SettingsScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings = viewModel.settings.collectAsStateWithLifecycle(Settings(emptyList(), emptyList()))

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Text("Routes")
            LazyColumn() {
                items(settings.value.vpnRoutes) {
                    CidrCard(it, { Timber.d("on delete") })
                }
            }

            Text("Addresses")
            LazyColumn() {
                items(settings.value.vpnAddresses) {
                    CidrCard(it, { Timber.d("on delete") })
                }
            }
        }
    }
}
