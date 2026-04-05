package io.github.heather7283.wolfram

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import io.github.heather7283.wolfram.WolframDestinationsArgs.CONFIG_NAME_ARG
import io.github.heather7283.wolfram.WolframDestinationsArgs.TITLE_ARG
import io.github.heather7283.wolfram.WolframScreens.ADD_EDIT_CONFIG_SCREEN
import io.github.heather7283.wolfram.WolframScreens.CONFIGS_SCREEN
import io.github.heather7283.wolfram.WolframScreens.SETTINGS_SCREEN
import io.github.heather7283.wolfram.data.XrayConfig
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings

private object WolframScreens {
    const val CONFIGS_SCREEN = "configs"
    const val SETTINGS_SCREEN = "settings"
    const val ADD_EDIT_CONFIG_SCREEN = "addEditConfig"
}

object WolframDestinationsArgs {
    const val CONFIG_NAME_ARG = "configName"
    const val TITLE_ARG = "title"
}

enum class WolframDestination(
    val onBar: Boolean,
    val route: String,
    val icon: ImageVector? = null,
    val label: String? = null,
) {
    CONFIGS(true, "configs", Icons.Default.Code, "Configs"),
    SETTINGS(true, "settings", Icons.Default.Settings, "Settings"),
    ADD_EDIT_CONFIG(false, "addEditConfig/{title}?configName={configName}"),
}

object WolframDestinations {
    const val CONFIGS_ROUTE = CONFIGS_SCREEN
    const val SETTINGS_ROUTE = SETTINGS_SCREEN
    const val ADD_EDIT_CONFIG_ROUTE = "${ADD_EDIT_CONFIG_SCREEN}/{${TITLE_ARG}}?${CONFIG_NAME_ARG}={${CONFIG_NAME_ARG}}"
}

class WolframNavigationActions(private val navController: NavHostController) {
    fun navigateToConfigs() {
        navController.navigate(WolframDestination.CONFIGS.route)
    }

    fun navigateToSettings() {
        navController.navigate(WolframDestination.SETTINGS.route)
    }

    fun navigateToAddEditConfig(title: String, config: XrayConfig?) {
        var route = "addEditConfig/${title}"
        if (config != null) route += "?configName=${config.name}"
        navController.navigate(route)
    }
}
