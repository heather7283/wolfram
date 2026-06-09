package io.github.heather7283.wolfram.ui.addeditconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditConfigScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: AddEditConfigViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (uiState.isModified) {
                FloatingActionButton(
                    onClick = { viewModel.saveConfig() },
                    shape = CircleShape,
                ) {
                    Icon(Icons.Default.Save, "Save config")
                }
            }
        },
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding),
        ) {
            TextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                singleLine = false,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}
