package com.su.moonlight.next.ui.main

import androidx.compose.ui.graphics.vector.ImageVector
import com.su.moonlight.next.NavDestination

interface MainNavigationItem : NavDestination {
    val icon: ImageVector
    override val route: String get() = ""
}