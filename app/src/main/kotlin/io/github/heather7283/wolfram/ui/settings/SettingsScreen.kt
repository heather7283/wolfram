package io.github.heather7283.wolfram.ui.settings

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.Either
import com.google.accompanist.drawablepainter.rememberDrawablePainter
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
private fun IntOption(
    title: String,
    value: Int,
    modified: Boolean,
    onValueChange: (Int) -> Unit,
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
                value = value.toString(),
                onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
private fun StringOption(
    string: String,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().height(24.dp),
    ) {
        Text(string)
        IconButton(onClick = { onDelete(string) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
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
private fun MultiChoiceOption(
    title: String,
    subtitle: String? = null,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, string ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { onSelect(index) },
                    selected = index == selected,
                    label = { Text(string) },
                    modifier = modifier,
                )
            }
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
private fun AppSelectPopup(
    title: String,
    apps: List<AppInfo>,
    onConfirm: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.padding(4.dp, 24.dp, 4.dp, 96.dp)) {
            Card {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(apps) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onConfirm(app) },
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    // are we for real? https://stackoverflow.com/a/78640640
                                    Image(
                                        painter = rememberDrawablePainter(app.icon),
                                        contentDescription = "Application icon",
                                        modifier = Modifier.size(36.dp),
                                    )
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = app.name,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            text = app.packageName,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
private fun SelectedAppsSection(
    apps: List<AppInfo>,
    isWhitelist: Boolean,
    onToggleWhitelist: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onDelete: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = "Application proxy",
        button = {
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add application")
            }
        },
        modifier = modifier,
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MultiChoiceOption(
                    title = "Mode",
                    options = listOf("Whitelist", "Blacklist"),
                    selected = if (isWhitelist) { 0 } else { 1 },
                    onSelect = { onToggleWhitelist(when (it) { 0 -> true; else -> false }) },
                )
            }
            apps.forEach { app ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Image(
                        painter = rememberDrawablePainter(app.icon),
                        contentDescription = "Application icon",
                        modifier = Modifier.size(36.dp),
                    )
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = app.packageName,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconButton(onClick = { onDelete(app) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove application")
                    }
                }
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
    val apps by vm.apps.collectAsStateWithLifecycle()

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
                        IntOption(
                            title = "Poll interval",
                            value = if (state.statsPollIntervalModified) {
                                state.statsPollIntervalValue
                            } else {
                                settings.statsPollInterval
                            },
                            modified = state.statsPollIntervalModified,
                            onValueChange = vm::onStatsPollIntervalValueChange,
                            onSave = vm::onStatsPollIntervalSave,
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

                SelectedAppsSection(
                    apps = apps.filter { it.selected },
                    isWhitelist = settings.selectedAppsIsWhitelist,
                    onToggleWhitelist = vm::setSelectedAppsIsWhitelist,
                    onAdd = {
                        vm.openAppsPopup(
                            title = "Select application",
                            onConfirm = { vm.addSelectedApp(it) },
                        )
                    },
                    onDelete = { vm.removeSelectedApp(it) },
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
        is SettingsPopup.Apps -> AppSelectPopup(
            title = popup.title,
            apps = apps.filter { !it.selected },
            onConfirm = popup.onConfirm,
            onDismiss = vm::closePopup,
        )
    }
}
