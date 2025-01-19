package com.su.moonlight.next.img

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import com.limelight.grid.assets.CachedAppAssetLoader.LoaderTuple
import com.limelight.grid.assets.DiskAssetLoader
import com.limelight.grid.assets.NetworkAssetLoader
import com.model.GameApp
import com.su.moonlight.next.App
import com.su.moonlight.next.utils.KLog
import java.util.Collections

class AppFetcher(app: GameApp) : Fetcher {

    private val log = KLog("AppFetcher")

    private val networkAssetLoaderPool =
        Collections.synchronizedMap(mutableMapOf<String, NetworkAssetLoader>())
    private val tuple = LoaderTuple(app.computer, app.nvApp)

    private fun getNetworkAssetLoader(uniqueId: String): NetworkAssetLoader {
        var networkAssetLoader = networkAssetLoaderPool[uniqueId]
        if (networkAssetLoader == null) {
            networkAssetLoader = NetworkAssetLoader(App.ins, uniqueId)
            networkAssetLoaderPool[uniqueId] = networkAssetLoader
        }
        return networkAssetLoader
    }


    private val decodeOnlyOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        inSampleSize = DiskAssetLoader.calculateInSampleSize(
            this,
            DiskAssetLoader.STANDARD_ASSET_WIDTH,
            DiskAssetLoader.STANDARD_ASSET_HEIGHT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            inPreferredConfig = Bitmap.Config.HARDWARE
        }
    }


    override suspend fun fetch(): FetchResult? {
        log.d("fetch game icon start: ${tuple.app.appId}")
        getNetworkAssetLoader(tuple.computer.localUniqueId).getBitmapStream(tuple)
            ?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, )?.let {
                    log.d("fetch game icon success: ${tuple.app.appId}")
                    return ImageFetchResult(
                        image = it.asImage(),
                        isSampled = false,
                        dataSource = DataSource.DISK
                    )
                }
            }
        log.d("fetch game icon error: ${tuple.app.appId}")
        return null
    }

    class Factory : Fetcher.Factory<GameApp> {
        override fun create(data: GameApp, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppFetcher(data)
        }
    }

}