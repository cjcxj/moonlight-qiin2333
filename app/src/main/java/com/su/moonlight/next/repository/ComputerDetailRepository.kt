package com.su.moonlight.next.repository

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.limelight.binding.crypto.AndroidCryptoProvider
import com.limelight.computers.ComputerManagerService
import com.limelight.computers.ComputerManagerService.ComputerManagerBinder
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvApp
import com.limelight.nvstream.http.NvHTTP
import com.limelight.nvstream.http.PairingManager
import com.limelight.utils.CacheHelper
import com.su.moonlight.next.utils.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.util.Locale
import kotlin.coroutines.resume

object ComputerDetailRepository {

    private val kLog = KLog("ComputerDetailRepository")

    fun getComputerDetailFlow(activity: Activity): Flow<ComputerDetails?> {
        kLog.d("getComputerDetailFlow: act=$activity")
        return callbackFlow<ComputerDetails> {
            var serviceConnection: ServiceConnection? = null
            val binder = suspendCancellableCoroutine<ComputerManagerBinder> {
                serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(className: ComponentName, binder: IBinder) {
                        val localBinder = (binder as ComputerManagerBinder)
                        launch {
                            withContext(Dispatchers.IO) { localBinder.waitForReady() }
                            it.resume(localBinder)
                            // Force a keypair to be generated early to avoid discovery delays
                            AndroidCryptoProvider(activity).clientCertificate
                        }
                    }

                    override fun onServiceDisconnected(className: ComponentName) {
                        cancel()
                    }
                }
                activity.bindService(
                    Intent(activity, ComputerManagerService::class.java),
                    serviceConnection!!,
                    Service.BIND_AUTO_CREATE
                )
            }

            binder.startPolling {
                kLog.d("update: (${it.hashCode()})$it")
                it.localUniqueId = binder.uniqueId
                trySend(it)
            }

            awaitClose {
                //serviceConnection?.let { activity.unbindService(it) }
            }
        }.distinctUntilChanged()
    }

    fun getComputerAppListFlow(activity: Activity, uuid: String): Flow<List<NvApp>?> {
        var lastDetails: ComputerDetails? = null
        var lastApps: String? = null
        return getComputerDetailFlow(activity)
            .onEach {
                KLog.common.d("app: ${it?.rawAppList}")
            }
            .filter { it?.rawAppList != lastApps }
            .filter { it != lastDetails }
            .filter { it?.uuid?.lowercase(Locale.ENGLISH) == uuid }
            .filter { it?.state == ComputerDetails.State.ONLINE }
            .filter { it?.pairState == PairingManager.PairState.PAIRED }
            .map { details ->
                lastDetails = details
                lastApps = details?.rawAppList
                CacheHelper.readInputStreamToString(
                    CacheHelper.openCacheFileForInput(
                        activity.getCacheDir(),
                        "applist",
                        uuid
                    )
                )?.let { NvHTTP.getAppListByReader(StringReader(it)) }
            }
    }
}