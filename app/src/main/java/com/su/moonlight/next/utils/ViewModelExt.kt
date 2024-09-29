package com.su.moonlight.next.utils

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

@Composable
inline fun <reified VM : ViewModel> collectViewModel(): VM {
    val context = LocalContext.current as ComponentActivity
    val vm by remember {
        mutableStateOf(context.viewModels<VM>().value)
    }
    return vm
}