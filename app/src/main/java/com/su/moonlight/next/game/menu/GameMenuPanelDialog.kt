package com.su.moonlight.next.game.menu

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.Game
import com.su.moonlight.next.App
import com.su.moonlight.next.LocalComposeDialogController
import com.su.moonlight.next.base.ComposeDialog
import com.su.moonlight.next.game.menu.options.MenuOption
import com.su.moonlight.next.game.menu.options.MenuPanelOption

fun showMenuPanelDialog(
    game: Game,
    title: String,
    options: List<MenuPanelOption>,
    runner: (Runnable?) -> Unit
) {
    ComposeDialog {
        GameMenuPanel(
            title,
            options,
            if (App.ins.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 4 else 7,
            runner
        )
    }.show(game.supportFragmentManager, "")
}

@Preview
@Composable
private fun Preview() {
    val ops = mutableListOf<MenuPanelOption>()
    repeat(10) {
        ops.add(MenuOption("TileTest$it", null))
    }
    GameMenuPanel("Start", ops, 4) { }
}

@Composable
private fun GameMenuPanel(
    title: String,
    options: List<MenuPanelOption>,
    spanCount: Int,
    runner: (Runnable?) -> Unit
) {
    val dialogController = LocalComposeDialogController.current
    val padding = 4.dp
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = padding),
            text = title,
            color = Color.White, fontSize = 24.sp
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanCount),
            contentPadding = PaddingValues(padding),
            verticalArrangement = Arrangement.spacedBy(padding),
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            options.forEach {
                item {
                    Box(modifier = Modifier.clickable {
                        if (it.isAutoCloseMenu) {
                            dialogController.dismissDialog()
                        }
                        runner(it.runnable)
                    }) {
                        it.MenuUI()
                    }
                }
            }
        }
    }
}