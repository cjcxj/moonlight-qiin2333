package com.su.moonlight.next

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.su.moonlight.next.base.ComposeDialogController

val LocalComposeDialogController = compositionLocalOf<ComposeDialogController> {
    object : ComposeDialogController {
        override fun dismissDialog() {
            TODO("Not yet implemented")
        }
    }
}

val LocalNavDestination = compositionLocalOf<NavDestination> {
    object : NavDestination {
        override val route: String
            get() = TODO("Not yet implemented")

        @Composable
        override fun Screen() {
            TODO("Not yet implemented")
        }

    }
}

val LocalNavDestinationArgs = compositionLocalOf<Bundle?> {
    null
}

val LocalNavHostController = compositionLocalOf<NavHostController?> { null }