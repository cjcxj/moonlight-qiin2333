package com.su.moonlight.next.ui.game

import android.provider.Contacts.Intents.UI
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limelight.AppView
import com.su.moonlight.next.LocalNavDestination
import com.su.moonlight.next.LocalNavDestinationArgs
import com.su.moonlight.next.LocalNavHostController
import com.su.moonlight.next.NavDestination
import com.su.moonlight.next.ui.main.play.PlayViewModel
import com.su.moonlight.next.utils.KLog
import com.su.moonlight.next.utils.collectViewModel
import org.bouncycastle.math.raw.Mod

object GameDestination : NavDestination {
    override val route: String = "GameRoute/{${AppView.NAME_EXTRA}}/{${AppView.UUID_EXTRA}}"
    override val title: String
        @Composable
        get() = "Game"

    val name: String?
        @Composable
        get() {
            val args = LocalNavDestinationArgs.current
            return args?.getString(AppView.NAME_EXTRA)
        }

    val uuid: String?
        @Composable
        get() {
            val args = LocalNavDestinationArgs.current
            return args?.getString(AppView.UUID_EXTRA)
        }


    @Composable
    override fun Screen() {
        Game()
    }

    fun route(app: String, uuid: String) =
        route.replace("{${AppView.NAME_EXTRA}}", app).replace("{${AppView.UUID_EXTRA}}", uuid)


    override fun toString(): String {
        return route
    }
}

@Preview
@Composable
private fun Preview() {
    Game()
}

@Composable
private fun Game() {
    val navController = LocalNavHostController.current
    val destination = LocalNavDestination.current as GameDestination
    val vm = collectViewModel<PlayViewModel>()
    val apps = vm.collectAppListState(uuid = destination.uuid!!)
    RoundDialog {
        DialogHead(title = destination.name!!) {
            CloseAction {
                navController?.popBackStack()
            }
        }
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(3),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            apps?.let { nvApps ->
//                items(nvApps) {
//                    GameItem(app = it)
//                }
//            }
//        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            apps?.let { it ->
                items(it) {
                    GameItem(app = it)
                }
            }
        }
    }
}

@Composable
private fun RoundDialog(content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(), containerColor = Color.Gray
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .fillMaxSize()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun DialogHead(title: String, action: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium,
            text = title
        )
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            action()
        }
    }
}

@Composable
private fun CloseAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFF2F2F4))
        ) {
            Icon(
                modifier = Modifier
                    .scale(0.6F)
                    .padding(2.dp),
                imageVector = Icons.Rounded.Close,
                contentDescription = "",
                tint = Color(0xFF9296A1)
            )
        }
    }
}