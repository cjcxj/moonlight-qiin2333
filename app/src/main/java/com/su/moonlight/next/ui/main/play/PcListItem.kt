package com.su.moonlight.next.ui.main.play

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DoDisturbAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.limelight.AppView
import com.limelight.nvstream.http.ComputerDetails
import com.su.moonlight.next.LocalNavHostController
import com.su.moonlight.next.ui.game.GameDestination
import com.su.moonlight.next.utils.safeToSting

@Composable
@Preview
private fun Preview() {
    Column(modifier = Modifier.background(Color.White)) {
        PcListItem(computer = ComputerDetails().apply {
            state = ComputerDetails.State.ONLINE
        })
        PcListItem(computer = ComputerDetails().apply {
            state = ComputerDetails.State.OFFLINE
        })
        PcListItem(computer = ComputerDetails().apply {
            state = ComputerDetails.State.UNKNOWN
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PcListItem(
    modifier: Modifier = Modifier,
    computer: ComputerDetails
) {
    val context = LocalContext.current
    val navController = LocalNavHostController.current
    val alpha by animateFloatAsState(
        when (computer.state) {
            ComputerDetails.State.ONLINE -> 1F
            else -> 0.45F
        }, label = ""
    )
    val color by animateColorAsState(
        when (computer.state) {
            ComputerDetails.State.ONLINE, ComputerDetails.State.OFFLINE -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.errorContainer
        }, label = ""
    )
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = {
                    if (computer.state == ComputerDetails.State.ONLINE)
                        navController?.navigate(GameDestination.route(computer.name, computer.uuid))
                },
                onLongClick = {

                }
            )
            .clip(CardDefaults.shape)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier
                        .scale(1.5F)
                        .padding(horizontal = 6.dp),
                    imageVector = Icons.Rounded.Computer,
                    contentDescription = ""
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = computer.name.safeToSting(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = computer.state.name.safeToSting().lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                IconButton(
                    onClick = {
                        context.launchAppList(computer)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector =
                        if (computer.state == ComputerDetails.State.ONLINE) Icons.Rounded.PlayArrow
                        else Icons.Rounded.DoDisturbAlt,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = computer.activeAddress.safeToSting(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            )
            Text(
                text = computer.uuid.safeToSting(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Context.launchAppList(computer: ComputerDetails) {
    val i = Intent(this, AppView::class.java)
    i.putExtra(AppView.NAME_EXTRA, computer.name)
    i.putExtra(AppView.UUID_EXTRA, computer.uuid)
    i.putExtra(AppView.NEW_PAIR_EXTRA, false)
    i.putExtra(AppView.SHOW_HIDDEN_APPS_EXTRA, false)
    startActivity(i)
}