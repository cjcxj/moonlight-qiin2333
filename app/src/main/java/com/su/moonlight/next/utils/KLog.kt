package com.su.moonlight.next.utils

import android.util.Log
import com.su.moonlight.next.BuildConfig

class KLog(private val tag: String) {
    var printInRelease = true

    private inline fun onDeBug(block: () -> Unit) {
        if (printInRelease || BuildConfig.DEBUG) {
            block()
        }
    }

    fun d(msg: String) {
        onDeBug {
            Log.d(tag, msg)
        }
    }

    fun i(msg: String) {
        onDeBug {
            Log.i(tag, msg)
        }
    }

    fun e(msg: String) {
        onDeBug {
            Log.e(tag, msg)
        }
    }
}