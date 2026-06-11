package io.github.heather7283.wolfram.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.Either
import io.github.heather7283.wolfram.ui.WolframNavigationActions
import io.github.heather7283.wolfram.utils.CIDR

@Composable
fun ToggleOption(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun TextOption(
    title: String,
    value: String,
    modified: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    placeholder: String = "",
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            if (modified) {
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = onSave) {
                    Icon(Icons.Default.Save, "Save")
                }
            }
        }
    }
}

@Composable
private fun CidrOption(
    cidr: CIDR,
    onDelete: (cidr: CIDR) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().height(24.dp),
    ) {
        Text("${cidr.ip.hostAddress}/${cidr.prefix}")
        IconButton(onClick = { onDelete(cidr) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
private fun CidrInputPopup(
    title: String,
    initialIp: String?,
    initialPrefix: Int?,
    onConfirm: (cidr: CIDR) -> Unit,
    onDismiss: () -> Unit,
) {
    var ip by rememberSaveable { mutableStateOf(initialIp) }
    var prefix by rememberSaveable { mutableStateOf(initialPrefix) }

    val cidr = if (ip != null && prefix != null) {
        CIDR.fromIpAndPrefix(ip!!, prefix!!)
    } else {
        Either.Left("Empty IP or prefix")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = ip ?: "",
                    onValueChange = { ip = it.trim() },
                    label = { Text("Address") },
                    singleLine = true,
                    isError = cidr.isLeft(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.weight(0.67f),
                )
                Text("/", fontSize = TextUnit(24f, TextUnitType.Sp))
                OutlinedTextField(
                    value = prefix?.toString(10) ?: "",
                    onValueChange = { prefix = it.toIntOrNull(10) ?: prefix },
                    label = { Text("Prefix") },
                    singleLine = true,
                    isError = cidr.isLeft(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.33f),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { cidr.onRight { onConfirm(it) } },
                enabled = cidr.isRight(),
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    button: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.fillMaxWidth().height(24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (button != null) {
                button()
            }
        }
        content()
    }
}

@Composable
private fun VpnAddressesSection(
    addresses: List<CIDR>,
    onAdd: () -> Unit,
    onDelete: (CIDR) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = "VPN addresses",
        button = {
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add VPN address")
            }
        },
        modifier = modifier,
    ) {
        Column {
            addresses.forEach {
                CidrOption(cidr = it, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun VpnRoutesSection(
    routes: List<CIDR>,
    onAdd: () -> Unit,
    onDelete: (CIDR) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = "VPN routes",
        button = {
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add VPN route")
            }
        },
        modifier = modifier,
    ) {
        Column {
            routes.forEach {
                CidrOption(cidr = it, onDelete = onDelete)
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
    val vm: SettingsViewModel = hiltViewModel()
    val settings by vm.settings.collectAsStateWithLifecycle(null)
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            settings?.also { settings ->
                SettingsSection(title = "Stats") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleOption(
                            title = "Enable stats",
                            subtitle = "Enable collection of up/downlink stats from xray",
                            checked = settings.statsEnabled,
                            onCheckedChange = { vm.setStatsEnabled(it) },
                        )
                        TextOption(
                            title = "Stats endpoint",
                            value = if (state.statsEndpointModified) {
                                state.statsEndpointText
                            } else {
                                settings.statsEndpoint
                            },
                            modified = state.statsEndpointModified,
                            onValueChange = { vm.onStatsEndpointValueChange(it) },
                            onSave = { vm.onStatsEndpointSave() },
                        )
                    }
                }

                HorizontalDivider()

                // TODO: this is very very ass, make it better
                VpnAddressesSection(
                    addresses = settings.vpnAddresses,
                    onAdd = {
                        vm.openCidrPopup(
                            title = "Add VPN address",
                            onConfirm = { vm.addVpnAddress(it) }
                        )
                    },
                    onDelete = { vm.removeVpnAddress(it) },
                )

                HorizontalDivider()

                VpnRoutesSection(
                    routes = settings.vpnRoutes,
                    onAdd = {
                        vm.openCidrPopup(
                            title = "Add VPN route",
                            onConfirm = { vm.addVpnRoute(it) }
                        )
                    },
                    onDelete = { vm.removeVpnRoute(it) },
                )

                HorizontalDivider()
            }
        }
    }

    when (val popup = state.popup) {
        is SettingsPopup.Inactive -> Unit
        is SettingsPopup.Cidr -> CidrInputPopup(
            title = popup.title,
            initialIp = popup.ip,
            initialPrefix = popup.prefix,
            onConfirm = popup.onConfirm,
            onDismiss = vm::closePopup,
        )
    }
}
