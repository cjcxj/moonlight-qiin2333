package com.su.moonlight.next.ui.about

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.limelight.PcView

@Composable
@Preview
private fun Preview() {
    About()
}

@Composable
fun About() {
    var testInt by remember {
        mutableIntStateOf(1)
    }
    val context = LocalContext.current
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        testInt++
                        context.startActivity(Intent(context, PcView::class.java))
                    },
                text = "点击这里回到旧版，新版UI很不完善",
                fontSize = 24.sp
            )
        }
    }
}