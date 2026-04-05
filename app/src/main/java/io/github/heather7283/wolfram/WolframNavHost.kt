package io.github.heather7283.wolfram

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.heather7283.wolfram.ui.configs.ConfigsScreen
import io.github.heather7283.wolfram.ui.settings.SettingsScreen
import timber.log.Timber

@Composable
fun WolframNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navActions = remember(navController) { WolframNavigationActions(navController) }
    val startDestination = WolframDestination.entries.first { it.onBar }
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        val bar = @Composable {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                WolframDestination.entries.filter { it.onBar }.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination.ordinal,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = destination.ordinal
                        },
                        icon = { Icon(destination.icon!!, contentDescription = destination.label!!) },
                        label = { Text(destination.label!!) },
                    )
                }
            }
        }
        WolframDestination.entries.filter { it.onBar }.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    WolframDestination.CONFIGS -> ConfigsScreen(bar, navActions)
                    WolframDestination.SETTINGS -> SettingsScreen(bar, navActions)
                    else -> {}
                }
            }
        }
    }
}
