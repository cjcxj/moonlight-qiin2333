package com.su.moonlight.next.ui.main.play

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.su.moonlight.next.LocalNavDestination
import com.su.moonlight.next.R
import com.su.moonlight.next.ui.main.MainNavigationItem
import com.su.moonlight.next.ui.main.MainTopAppBar
import com.su.moonlight.next.utils.collectViewModel

object PlayNavigationItem : MainNavigationItem {

    override val icon: ImageVector = Icons.Rounded.Games

    override val titleRes: Int = R.string.app_play

    @Composable
    override fun Screen() {
        Play()
    }

}

@Composable
@Preview
private fun Preview() {
    Play()
}

@Composable
private fun Play() {
    val item = LocalNavDestination.current
    val vm = collectViewModel<PlayViewModel>()
    val list by vm.collectComputerListState()
    Scaffold(topBar = {
        PlayTopAppBar(
            title = {
                Text(text = "设备列表", style = MaterialTheme.typography.titleMedium)
            },
            subTitle = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "当前共${list.size}个，正在扫描",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            },
            actionIcon = Icons.Rounded.Add
        )
    }) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn {
                item {
                    Spacer(modifier = Modifier.size(8.dp))
                }
                list.forEach {
                    item {
                        PcListItem(computer = it)
                    }
                }
            }
        }
    }
}