package com.su.moonlight.next.ui.main

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.limelight.preferences.StreamSettings
import com.su.moonlight.next.LocalNavHostController
import com.su.moonlight.next.NavDestination
import com.su.moonlight.next.base.FragmentLifecycleScope
import com.su.moonlight.next.ui.about.AboutDestination
import com.su.moonlight.next.ui.main.play.PlayNavigationItem
import com.su.moonlight.next.ui.main.settings.SettingsNavigationItem
import kotlinx.coroutines.launch

object MainDestination : NavDestination {
    override val route: String = "MainRoute"
    override val title: String
        @Composable
        get() = "Main"

    @Composable
    override fun Screen() {
        Main()
    }

    override fun toString(): String {
        return route
    }
}

private val navigationItems = listOf(
    SettingsNavigationItem,
    PlayNavigationItem,
    AboutDestination
)
private val initIndex = navigationItems.size / 2

@Composable
@Preview
private fun Preview() {
    Main()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Main() {
    var currentIndex by remember { mutableIntStateOf(initIndex) }
    val pagerState = rememberPagerState(initialPage = initIndex) {
        navigationItems.size
    }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
//        topBar = {
//           MainTopAppBar(
//               title = navigationItems[currentIndex].itemName,
//               navigationIcon = Icons.Rounded.Menu,
//               navigationIconContentDescription = "",
//               actionIcon = Icons.Rounded.ArrowBackIosNew,
//               actionIconContentDescription = ""
//           )
//        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, navigationItem ->
                    NavigationBarItem(
                        selected = index == currentIndex,
                        onClick = {
                            currentIndex = index
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = navigationItem.icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = navigationItem.title)
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { paddingValues ->
        FragmentLifecycleScope(fragment = StreamSettings.SettingsFragment()) {
            Box(
                modifier = Modifier
                    .padding(bottom = paddingValues.calculateBottomPadding())
//                .padding(paddingValues)
//                .consumeWindowInsets(paddingValues)
            ) {
                HorizontalPager(
                    state = pagerState,
                    key = { index -> index.toString() },
                    userScrollEnabled = false
                ) {
                    navigationItems[it].CallScreen()
                }
            }
        }
    }
}