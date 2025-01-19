package com.su.moonlight.next.img

import coil3.ImageLoader
import coil3.SingletonImageLoader

object CoilInit {
    fun init() {
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(it).components {
                add(AppFetcher.Factory())
                add(AppKeyer())
            }.build()
        }
    }
}