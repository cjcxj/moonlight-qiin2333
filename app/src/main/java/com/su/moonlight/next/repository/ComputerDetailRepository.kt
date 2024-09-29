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
import com.su.moonlight.next.utils.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
                kLog.d("update: $it")
                trySend(it)
            }

            awaitClose {
                serviceConnection?.let { activity.unbindService(it) }
            }
        }
    }
}