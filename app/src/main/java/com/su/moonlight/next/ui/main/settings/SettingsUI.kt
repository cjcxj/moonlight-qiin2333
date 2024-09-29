package com.su.moonlight.next.ui.main.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.su.moonlight.next.LocalNavDestination
import com.su.moonlight.next.R
import com.su.moonlight.next.base.Fragment
import com.su.moonlight.next.ui.main.MainNavigationItem

object SettingsNavigationItem : MainNavigationItem {

    override val icon: ImageVector = Icons.Rounded.Settings

    override val titleRes: Int = R.string.app_settings

    @Composable
    override fun Screen() {
        Settings()
    }

}

@Composable
@Preview
private fun Preview() {
    Settings()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings() {
    val item = LocalNavDestination.current
    var exp by remember {
        mutableStateOf(false)
    }
    Scaffold() { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ExposedDropdownMenuBox(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                expanded = false,
                onExpandedChange = {
                    exp = it
                }) {
                SettingPrefChoose(modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(
                    expanded = exp,
                    onDismissRequest = { exp = false },
                ) {

                    DropdownMenuItem(
                        text = { Text("option", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            exp = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )

                }
            }
            //TODO 这里因为frg和view复用，存在滑动位置问题。后面直接写新的所以就不修了
            Fragment(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
private fun SettingPrefChoose(modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start)
            ) {
                Text(
                    text = stringResource(id = R.string.app_pref_current_config),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.app_pref_default_config),
                        fontSize = 16.sp
                    )
                    Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = "")
                }
            }

            IconButton(modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End), onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "")
            }
        }
    }
}