package com.su.moonlight.next

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        CompositionLocalProvider(
            LocalNavDestination provides navDestination,
            LocalNavDestinationArgs provides it.arguments
        ) {
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
            builder = builder,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Up
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Down
                )
            }
        )
    }
}