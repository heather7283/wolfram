package io.github.heather7283.wolfram.ui.logs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.heather7283.wolfram.WolframNavigationActions
import kotlinx.coroutines.launch

@Composable
fun LogsScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    val viewModel: LogsViewModel = hiltViewModel()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val logLen = viewModel.log.size
    LaunchedEffect(logLen) {
        if (logLen > 0) {
            listState.scrollToItem(logLen - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
        floatingActionButton = FAB@{
            if (!listState.canScrollForward) {
                return@FAB
            }

            FloatingActionButton(
                { coroutineScope.launch { listState.scrollToItem(logLen - 1) } },
                shape = CircleShape,
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    "Scroll to the bottom",
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
