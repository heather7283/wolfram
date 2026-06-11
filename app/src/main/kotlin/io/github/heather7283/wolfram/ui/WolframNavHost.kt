package io.github.heather7283.wolfram.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.heather7283.wolfram.ui.addeditconfig.AddEditConfigScreen
import io.github.heather7283.wolfram.ui.assets.AssetsScreen
import io.github.heather7283.wolfram.ui.configs.ConfigsScreen
import io.github.heather7283.wolfram.ui.dashboard.DashboardScreen
import io.github.heather7283.wolfram.ui.logs.LogsScreen
import io.github.heather7283.wolfram.ui.settings.SettingsScreen

@Composable
fun WolframNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navActions = remember(navController) { WolframNavigationActions(navController) }
    val startDestination = WolframTopLevelDestination.entries.first()
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier.imePadding(),
    ) {
        val bar = @Composable {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                WolframTopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination.ordinal,
                        onClick = {
                            navActions.navigateToTopLevel(destination)
                            selectedDestination = destination.ordinal
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        }

        WolframTopLevelDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    WolframTopLevelDestination.DASHBOARD -> DashboardScreen(bar, navActions)
                    WolframTopLevelDestination.LOGS -> LogsScreen(bar, navActions)
                    WolframTopLevelDestination.CONFIGS -> ConfigsScreen(bar, navActions)
                    WolframTopLevelDestination.SETTINGS -> SettingsScreen(bar, navActions)
                    WolframTopLevelDestination.ASSETS -> AssetsScreen(bar, navActions)
                }
            }
        }

        composable(
            "addEditConfig/{title}?configId={configId}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("configId") { type = NavType.LongType },
            )
        ) { entry ->
            val title = entry.arguments?.getString("title")!!
            AddEditConfigScreen(title)
        }
    }
}
