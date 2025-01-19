package com.su.moonlight.next

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.su.moonlight.next.ui.about.AboutDestination
import com.su.moonlight.next.ui.game.GameDestination
import com.su.moonlight.next.ui.main.MainDestination

class HostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen()
        setContent {
            MoonlightNextTheme {
                MoonlightNextNavHost(
                    startDestination = MainDestination.route
                ) {
                    composableNavDestination(MainDestination)
                    composableNavDestination(AboutDestination)
                    composableNavDestination(GameDestination)
                }
            }
        }
    }

    private fun fullScreen() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
    }
}