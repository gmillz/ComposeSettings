package com.gmillz.compose.settings.ui

import androidx.annotation.StringRes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmillz.compose.settings.ui.components.ProvideBottomSheetHandler
import com.gmillz.compose.settings.util.LocalNavController
import com.gmillz.compose.settings.util.ProviderLifecycleState

@Composable
fun SettingsSurface(
    startDestination: String,
    screens: List<SettingsScreen>
) {
    val navController = rememberNavController()
    ProviderLifecycleState {
        ProvideBottomSheetHandler {
            Surface {
                CompositionLocalProvider(
                    LocalNavController provides navController
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        loadScreens(screens)
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.loadScreens(screens: List<SettingsScreen>) {
    screens.forEach { screen ->
        composable(
            route = screen.route,
            arguments = screen.arguments
        ) {
            screen.composable(it)
        }
        if (screen.screens.isNotEmpty()) {
            loadScreens(screen.screens)
        }
    }
}

data class SettingsScreen(
    val route: String,
    @StringRes val labelRes: Int? = null,
    val screens: List<SettingsScreen> = emptyList(),
    val arguments: List<NamedNavArgument> = emptyList(),
    val composable: @Composable (NavBackStackEntry) -> Unit)