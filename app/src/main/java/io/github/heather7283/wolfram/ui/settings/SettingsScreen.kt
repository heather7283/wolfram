package io.github.heather7283.wolfram.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.heather7283.wolfram.WolframNavigationActions

@Composable
fun SettingsScreen(
    navBar: @Composable () -> Unit,
    navActions: WolframNavigationActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = navBar,
    ) { contentPadding ->
        Text("TODO", modifier = Modifier.padding(contentPadding))
    }
}
