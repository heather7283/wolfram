package io.github.heather7283.wolfram.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.Either
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.github.heather7283.wolfram.data.template.Template
import io.github.heather7283.wolfram.ui.WolframNavigationActions
import io.github.heather7283.wolfram.utils.CIDR
import java.net.InetAddress

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
            .toggleable(
                value = checked,
                enabled = enabled,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
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
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (modified) {
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
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            if (modified) {
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
        modifier = modifier.fillMaxWidth(),
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
        modifier = modifier.fillMaxWidth(),
    ) {
        Text("${cidr.ip.hostAddress}/${cidr.prefix}")
        IconButton(onClick = { onDelete(cidr) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
private fun IpOption(
    ip: InetAddress,
    onDelete: (ip: InetAddress) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(ip.hostAddress)
        IconButton(onClick = { onDelete(ip) }) {
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
                Text("/", style = MaterialTheme.typography.headlineSmall)
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
                onClick = { cidr.onRight(onConfirm) },
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
private fun IpInputPopup(
    title: String,
    initialIp: String?,
    onConfirm: (ip: InetAddress) -> Unit,
    onDismiss: () -> Unit,
) {
    var ip by rememberSaveable { mutableStateOf(initialIp) }

    val inetAddr = if (ip != null) {
        Either.catch { InetAddress.getByName(ip!!) }
    } else {
        Either.Left("Empty IP")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = ip ?: "",
                onValueChange = { ip = it.trim() },
                label = { Text("Address") },
                singleLine = true,
                isError = inetAddr.isLeft(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            )
        },
        confirmButton = {
            Button(
                onClick = { inetAddr.onRight(onConfirm) },
                enabled = inetAddr.isRight(),
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
private fun TemplateInputPopup(
    title: String,
    id: Int?,
    initialKey: String?,
    initialReplacement: String?,
    onConfirm: (id: Int?, key: String, replacement: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var key by rememberSaveable { mutableStateOf(initialKey ?: "") }
    var replacement by rememberSaveable { mutableStateOf(initialReplacement ?: "") }

    val error = if (key.isBlank()) {
        "Empty key"
    } else if (replacement.isBlank()) {
        "Empty replacement"
    } else if (key.contains('@')) {
        "Key must not contain '@'"
    } else if (key.startsWith("WOLFRAM_")) {
        "Keys starting with WOLFRAM_ are reserved"
    } else {
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.trim() },
                    label = { Text("Key") },
                    singleLine = true,
                    isError = error != null,
                )
                OutlinedTextField(
                    value = replacement,
                    onValueChange = { replacement = it.trim() },
                    label = { Text("Replacement") },
                    singleLine = true,
                    isError = error != null,
                )
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(id, key, replacement) },
                enabled = error == null,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Card(
                        onClick = { onConfirm(app) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp),
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ErrorPopup(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        dismissButton = { /* no-op */ },
    )
}

@Composable
private fun RestartPopup(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restart") },
        text = { Text("Restart application to apply new settings") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        dismissButton = { /* no-op */ },
    )
}

@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    button: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (button != null) {
                    button()
                }
            }
            content()
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onAdd),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add $title",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun EmptyListHint() {
    Text(
        text = "Empty, tap + to add",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 12.dp),
    )
}

@Composable
private fun VpnSettingsSection(
    addresses: List<CIDR>,
    routes: List<CIDR>,
    isMetered: Boolean,
    dnsAddresses: List<InetAddress>,
    onAddAddress: () -> Unit,
    onDeleteAddress: (CIDR) -> Unit,
    onAddRoute: () -> Unit,
    onDeleteRoute: (CIDR) -> Unit,
    onSetMetered: (Boolean) -> Unit,
    onAddDns: () -> Unit,
    onDeleteDns: (InetAddress) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(title = "VPN settings", modifier = modifier) {
        Column {
            ToggleOption(
                title = "Metered connection",
                subtitle = "Treat the VPN connection as metered; if false, metered status of the underlying network will be inherited",
                checked = isMetered,
                onCheckedChange = onSetMetered,
            )

            GroupHeader(title = "VPN addresses", onAdd = onAddAddress)
            if (addresses.isEmpty()) {
                EmptyListHint()
            } else {
                addresses.forEachIndexed { index, cidr ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    CidrOption(cidr = cidr, onDelete = onDeleteAddress)
                }
            }

            GroupHeader(
                title = "VPN routes",
                onAdd = onAddRoute,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (routes.isEmpty()) {
                EmptyListHint()
            } else {
                routes.forEachIndexed { index, cidr ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    CidrOption(cidr = cidr, onDelete = onDeleteRoute)
                }
            }

            GroupHeader(
                title = "DNS addresses",
                onAdd = onAddDns,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (dnsAddresses.isEmpty()) {
                EmptyListHint()
            } else {
                dnsAddresses.forEachIndexed { index, ip ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    IpOption(ip = ip, onDelete = onDeleteDns)
                }
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
        modifier = modifier,
    ) {
        Column {
            ToggleOption(
                title = "Whitelist mode",
                subtitle = "If enabled, only selected apps use VPN; if disabled, selected apps bypass VPN",
                checked = isWhitelist,
                onCheckedChange = onToggleWhitelist,
            )
            GroupHeader(title = "Selected apps", onAdd = onAdd)
            if (apps.isEmpty()) {
                EmptyListHint()
            } else {
                apps.forEachIndexed { index, app ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Image(
                            painter = rememberDrawablePainter(app.icon),
                            contentDescription = "Application icon",
                            modifier = Modifier.size(36.dp),
                        )
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = app.name,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = app.packageName,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

@Composable
private fun TemplatesSection(
    templates: List<Template>,
    onAdd: () -> Unit,
    onEdit: (id: Int, key: String, replacement: String) -> Unit,
    onDelete: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSection(
        title = "Config templates",
        modifier = modifier,
        button = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAdd),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add config template",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    ) {
        Column {
            if (templates.isEmpty()) {
                EmptyListHint()
            } else {
                templates.forEachIndexed { index, template ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = template.key,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = template.replacement,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = { onEdit(template.id, template.key, template.replacement) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit template")
                        }
                        IconButton(
                            onClick = { onDelete(template.id) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove template")
                        }
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
    val templates by vm.templates.collectAsStateWithLifecycle(emptyList())

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.sqlite3"),
    ) { uri ->
        uri?.let { vm.onBackupLocationSelected(uri) }
    }
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { vm.onRestoreLocationSelected(uri) }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            settings?.also { settings ->
                SettingsSection(title = "Stats") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleOption(
                            title = "Enable stats",
                            subtitle = "Collect traffic statistics stats from xray",
                            checked = settings.statsEnabled,
                            onCheckedChange = vm::setStatsEnabled,
                        )
                        TextOption(
                            title = "Stats endpoint",
                            value = if (state.statsEndpointModified) {
                                state.statsEndpointText
                            } else {
                                settings.statsEndpoint
                            },
                            modified = state.statsEndpointModified,
                            onValueChange = vm::onStatsEndpointValueChange,
                            onSave = vm::onStatsEndpointSave,
                        )
                        IntOption(
                            title = "Poll interval (seconds)",
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

                VpnSettingsSection(
                    addresses = settings.vpnAddresses,
                    routes = settings.vpnRoutes,
                    isMetered = settings.vpnIsMetered,
                    dnsAddresses = settings.dnsAddresses,
                    onAddAddress = {
                        vm.openCidrPopup(
                            title = "Add VPN address",
                            onConfirm = vm::addVpnAddress,
                        )
                    },
                    onDeleteAddress = vm::removeVpnAddress,
                    onAddRoute = {
                        vm.openCidrPopup(
                            title = "Add VPN route",
                            onConfirm = vm::addVpnRoute,
                        )
                    },
                    onSetMetered = vm::setVpnIsMetered,
                    onDeleteRoute = vm::removeVpnRoute,
                    onAddDns = {
                        vm.openIpPopup(
                            title = "Add DNS address",
                            onConfirm = vm::addDnsAddress,
                        )
                    },
                    onDeleteDns = vm::removeDnsAddress,
                )

                SelectedAppsSection(
                    apps = apps.filter { it.selected },
                    isWhitelist = settings.selectedAppsIsWhitelist,
                    onToggleWhitelist = vm::setSelectedAppsIsWhitelist,
                    onAdd = {
                        vm.openAppsPopup(
                            title = "Select application",
                            onConfirm = vm::addSelectedApp,
                        )
                    },
                    onDelete = vm::removeSelectedApp,
                )

                TemplatesSection(
                    templates = templates,
                    onAdd = {
                        vm.openTemplatePopup(
                            title = "Add template",
                            onConfirm = vm::templateUpsert,
                        )
                    },
                    onEdit = { id, key, replacement ->
                        vm.openTemplatePopup(
                            title = "Edit template",
                            id = id,
                            key = key,
                            replacement = replacement,
                            onConfirm = vm::templateUpsert,
                        )
                    },
                    onDelete = vm::templateDelete,
                )

                SettingsSection(title = "Backup") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(
                            onClick = { createDocumentLauncher.launch("wolfram-settings.bak") }
                        ) {
                            Text("Backup settings")
                        }
                        Button(
                            onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }
                        ) {
                            Text("Restore settings")
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
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
        is SettingsPopup.Ip -> IpInputPopup(
            title = popup.title,
            initialIp = popup.ip,
            onConfirm = popup.onConfirm,
            onDismiss = vm::closePopup,
        )
        is SettingsPopup.Apps -> AppSelectPopup(
            title = popup.title,
            apps = apps.filter { !it.selected },
            onConfirm = popup.onConfirm,
            onDismiss = vm::closePopup,
        )
        is SettingsPopup.Template -> TemplateInputPopup(
            title = popup.title,
            id = popup.id,
            initialKey = popup.key,
            initialReplacement = popup.replacement,
            onConfirm = popup.onConfirm,
            onDismiss = vm::closePopup,
        )
        is SettingsPopup.Error -> ErrorPopup(
            title = popup.title,
            message = popup.message,
            onDismiss = vm::closePopup,
        )
        is SettingsPopup.Restart -> RestartPopup(
            onDismiss = vm::terminateApp,
        )
    }
}
