package io.github.heather7283.wolfram

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import io.github.heather7283.wolfram.data.ConfigFile

enum class WolframToplevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String,
) {
    DASHBOARD("dashboard", Icons.Default.Dashboard, "Dashboard"),
    CONFIGS("configs", Icons.Default.Code, "Configs"),
    SETTINGS("settings", Icons.Default.Settings, "Settings"),
}

class WolframNavigationActions(private val navController: NavHostController) {
    fun navigateToAddEditConfig(title: String, config: ConfigFile?) {
        var route = "addEditConfig/${title}"
        if (config != null) route += "?configName=${config.name}"
        navController.navigate(route)
    }
}
