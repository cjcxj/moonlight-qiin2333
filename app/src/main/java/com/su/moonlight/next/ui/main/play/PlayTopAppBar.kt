package com.su.moonlight.next.ui.main.play

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Preview() {
    PlayTopAppBar(
        title = { Text("Test") },
        subTitle = { Text("asdada") },
        actionIcon = Icons.Rounded.Info
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subTitle: @Composable (() -> Unit)? = null,
    actionIcon: ImageVector,
    onActionClick: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        title = {
            Column {
                title()
                subTitle?.let {
                    Spacer(modifier = Modifier.size(1.dp))
                    it.invoke()
                }
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}