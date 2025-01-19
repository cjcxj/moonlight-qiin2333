package com.su.moonlight.next.ui.game

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.limelight.utils.ServerHelper
import com.model.GameApp

@Composable
@Preview
private fun Preview() {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 2.dp,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            GameItem(GameApp.EMPTY)
        }
        item {
            GameItem(GameApp.EMPTY)
        }
        item {
            GameItem(GameApp.EMPTY)
        }
        item {
            GameItem(GameApp.EMPTY)
        }
    }

}

@Composable
fun GameItem(app: GameApp) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (context is Activity) {
                    ServerHelper.doStart(
                        context,
                        app.nvApp,
                        app.computer,
                        app.computer.localUniqueId,
                        false
                    )
                }
            }) {
        AsyncImage(
            model = app, contentDescription = null, modifier = Modifier
                .size(64.dp)
            //.clip(RoundedCornerShape(50F))
        )
        Text(text = app.nvApp.appName)
    }
}