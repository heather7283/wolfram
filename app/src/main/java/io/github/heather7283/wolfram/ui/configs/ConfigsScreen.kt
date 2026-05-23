package io.github.heather7283.wolfram.ui.configs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.WolframNavigationActions
import io.github.heather7283.wolfram.data.xrayconfig.XrayConfigData
import timber.log.Timber
import java.util.Collections.emptyList

@Composable
fun ConfigEntry(
    config: XrayConfigData,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = config.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = onEdit,
                // https://stackoverflow.com/a/66671903
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            FilledTonalButton(
                onClick = onDelete,
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun ConfigsScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: ConfigsViewModel = hiltViewModel()
    val configs = viewModel.configs.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navActions.navigateToAddEditConfig("Add config", null) },
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, "Add config")
            }
        },
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(contentPadding).fillMaxSize(),
        ) {
            if (configs.value.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        Text("Empty")
                    }
                }
            } else {
                items(configs.value) {
                    ConfigEntry(
                        config = it,
                        onClick = { Timber.d("${it.name} onClick clicked") },
                        onEdit = { navActions.navigateToAddEditConfig("Edit config", it) },
                        onDelete = { viewModel.delete(it.id) },
                    )
                }
            }
        }
    }
}
