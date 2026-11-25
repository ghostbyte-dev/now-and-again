package dev.ghostbyte.nowandagain.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.ghostbyte.nowandagain.ui.home.HomeScreen
import dev.ghostbyte.nowandagain.ui.settings.SettingsScreen

private val popEnter = slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))
private val popExit = slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(300))


@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        NavHost(
            navController = nav,
            startDestination = "home",
            enterTransition = { slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300)) },
            popEnterTransition = { popEnter },
            popExitTransition = { popExit },
            predictivePopEnterTransition = { popEnter },
            predictivePopExitTransition = { popExit },
        ) {
            composable("home") { HomeScreen(onOpenSettings = { nav.navigate("settings") }) }
            composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        }
    }
}