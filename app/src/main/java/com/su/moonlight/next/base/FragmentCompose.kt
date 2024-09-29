package com.su.moonlight.next.base

import android.app.Activity
import android.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.InteropView
import com.su.moonlight.next.R
import com.su.moonlight.next.utils.KLog

private val kLog = KLog("ComposeFragment")

/**
 * 需要配合[FragmentLifecycleScope]使用
 */
@Composable
fun Fragment(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (context is Activity) {
        val view = LocalFragmentView.current
        val frg = LocalFragment.current
        kLog.d("view: ${view.hashCode()}(${view.javaClass}), frg: ${frg.hashCode()}")
        SideEffect {
            val manager = context.fragmentManager
            if (!frg.isAdded) {
                kLog.d("replace")
                manager.beginTransaction().apply {
                    replace(R.id.fc_fragment, frg)
                    commitAllowingStateLoss()
                }
            }
        }
        AndroidView(factory = {
            kLog.d("create")
            view
        }, modifier = modifier,
            update = {
                kLog.d("update")
            }, onReset = {
                kLog.d("onReset")
            }, onRelease = {
                kLog.d("onRelease")
            })
    }
}

private val LocalFragment = compositionLocalOf<Fragment> { TODO() }
private val LocalFragmentView = compositionLocalOf<View> { TODO() }

@Composable
fun <F : Fragment> FragmentLifecycleScope(fragment: F, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view by remember {
        mutableStateOf(
            LayoutInflater.from(context)
                .inflate(R.layout.view_fragment_compose, null)
        )
    }
    CompositionLocalProvider(
        LocalFragment provides fragment,
        LocalFragmentView provides view,
        content = content
    )
}