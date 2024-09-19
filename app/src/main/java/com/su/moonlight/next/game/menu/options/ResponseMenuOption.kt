package com.su.moonlight.next.game.menu.options

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddChart
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.Game
import com.su.moonlight.next.App
import com.su.moonlight.next.R
import com.su.moonlight.next.defaultTileBackgroundColor
import com.su.moonlight.next.enablePerformanceOverlayTileBackgroundColor
import com.su.moonlight.next.enableZoomModeEnabledTileBackgroundColor

abstract class BaseResponseMenuOption(
    private val label: String,
    private val icon: ImageVector,
    private val isEnable: Boolean,
    private val respColor: Color,
    runnable: Runnable
) :
    MenuPanelOption(runnable) {

    @Composable
    override fun MenuUI() {
        val color = if (isEnable) Color.White else Color.Unspecified
        val backgroundColor =
            if (isEnable) respColor else defaultTileBackgroundColor
        CompositionLocalProvider(
            LocalContentColor provides color
        ) {
            MenuOptionTile(
                icon,
                label,
                backgroundColor
            )
        }
    }
}

class PerformanceOverlayMenuOption(game: Game) : BaseResponseMenuOption(
    App.ins.resources.getString(R.string.game_menu_hud),
    Icons.Rounded.AddChart,
    game.isEnablePerformanceOverlay,
    enablePerformanceOverlayTileBackgroundColor,
    Runnable { game.showHUD() }
) {

    private val showLite = Runnable { game.isLitePerfMode(true) }
    private val closeLite = Runnable { game.isLitePerfMode(false) }

    private fun isLitePerfMode(isLite: Boolean) {
        if (isLite) {
            showLite.run()
        } else {
            closeLite.run()
        }
    }

    private val isLiteModeState = mutableStateOf(game.isLitePerfMode)

    @Composable
    private fun singleCheckBoxBorderColor(color: Color): CheckboxColors {
        val default = CheckboxDefaults.colors()
        return remember {
            object : CheckboxColors by default {
                @Composable
                override fun borderColor(enabled: Boolean, state: ToggleableState): State<Color> {
                    return rememberUpdatedState(color)
                }
            }
        }
    }

    @Composable
    override fun MenuUI() {
        Box {
            super.MenuUI()
            LiteModeCheck(modifier = Modifier.align(Alignment.TopEnd))
        }
    }

    @Composable
    private fun LiteModeCheck(modifier: Modifier) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Lite", fontSize = 10.sp, color = Color.White)
            Checkbox(
                modifier = Modifier
                    .size(20.dp)
                    .scale(0.5F),
                checked = isLiteModeState.value,
                colors = singleCheckBoxBorderColor(color = Color.White),
                onCheckedChange = {
                    isLiteModeState.apply {
                        value = !value
                        isLitePerfMode(it)
                    }
                })
        }
    }
}

class PanZoomModeMenuOption(game: Game) : BaseResponseMenuOption(
    App.ins.resources.getString(if (game.isZoomModeEnabled) R.string.game_menu_disable_zoom_mode else R.string.game_menu_enable_zoom_mode),
    Icons.Rounded.PanTool,
    game.isZoomModeEnabled,
    enableZoomModeEnabledTileBackgroundColor,
    Runnable { game.toggleZoomMode() }
)