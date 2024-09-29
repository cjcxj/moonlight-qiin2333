package com.su.moonlight.next

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

interface NavDestination {
    val route: String

    @get:StringRes
    val titleRes: Int get() = 0

    val title: String
        @Composable
        get() = stringResource(titleRes)

    @Composable
    fun Screen()

    @Composable
    fun CallScreen() {
        CompositionLocalProvider(LocalNavDestination provides this) {
            Screen()
        }
    }
}

fun NavGraphBuilder.composableNavDestination(navDestination: NavDestination) {
    composable(route = navDestination.route) {
        CompositionLocalProvider(LocalNavDestination provides navDestination) {
            navDestination.CallScreen()
        }
    }
}

@Composable
fun MoonlightNextNavHost(startDestination: String, builder: NavGraphBuilder.() -> Unit) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavHostController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            builder = builder
        )
    }
}