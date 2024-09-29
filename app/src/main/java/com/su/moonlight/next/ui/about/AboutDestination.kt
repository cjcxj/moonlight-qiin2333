package com.su.moonlight.next.ui.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.su.moonlight.next.R
import com.su.moonlight.next.ui.main.MainNavigationItem

object AboutDestination : MainNavigationItem {

    override val icon: ImageVector = Icons.Rounded.Info
    override val route: String = "AboutRoute"

    override val titleRes: Int = R.string.app_about

    @Composable
    override fun Screen() {
        About()
    }
}