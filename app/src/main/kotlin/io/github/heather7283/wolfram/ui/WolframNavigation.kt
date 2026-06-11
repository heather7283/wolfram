package io.github.heather7283.wolfram.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import io.github.heather7283.wolfram.data.config.XrayConfigData

enum class WolframTopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String,
) {
    DASHBOARD("dashboard", Icons.Default.Dashboard, "Dashboard"),
    CONFIGS("configs", Icons.Default.Code, "Configs"),
    ASSETS("assets", Icons.Default.Category, "Assets"),
    LOGS("logs", Icons.Default.Notes, "Logs"),
    SETTINGS("settings", Icons.Default.Settings, "Settings"),
}

class WolframNavigationActions(private val navController: NavHostController) {
    fun navigateToTopLevel(dest: WolframTopLevelDestination) {
        navController.navigate(dest.route)
    }

    fun navigateToAddEditConfig(title: String, config: XrayConfigData?) {
        // java.lang.IllegalArgumentException: long does not allow nullable values :/
        navController.navigate("addEditConfig/${title}?configId=${config?.id ?: -228}")
    }
}
