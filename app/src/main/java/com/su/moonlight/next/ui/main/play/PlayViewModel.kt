package com.su.moonlight.next.ui.main.play

import android.app.Activity
import androidx.collection.ArrayMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvApp
import com.limelight.nvstream.http.NvHTTP
import com.limelight.nvstream.http.PairingManager
import com.limelight.utils.CacheHelper
import com.model.GameApp
import com.su.moonlight.next.repository.ComputerDetailRepository
import com.su.moonlight.next.utils.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.io.StringReader
import java.util.Locale

class PlayViewModel : ViewModel() {

    private val kLog = KLog("PlayViewModel").apply {
        printInRelease = false
    }
    private var computerList: ArrayMap<String, ComputerDetails> = ArrayMap()

    @Composable
    fun collectComputerListState(): State<Collection<ComputerDetails>> {
        val context = LocalContext.current as Activity

        val rawFlow by remember {
            mutableStateOf(
                ComputerDetailRepository.getComputerDetailFlow(context)
                    .map { computerDetails ->
                        if (computerDetails != null) {
                            computerList[computerDetails.uuid] = computerDetails
                            kLog.d("add comp(${computerDetails.hashCode()}): $computerDetails")
                        }
                        computerList.values.sortedBy { it.name }
                    }
                    .onEach {
                        kLog.d("update flow: ${it.hashCode()}")
                    }
                    .flowOn(Dispatchers.Default)
            )
        }

        return rawFlow.collectAsState(initial = emptyList<ComputerDetails>())
    }

    @Composable
    fun collectAppListState(uuid: String): List<GameApp>? {
        val computers by collectComputerListState()
        val context = LocalContext.current
        KLog.common.d("collect: $uuid")
        KLog.common.d("computers=${computers.joinToString { it.uuid }}")
        val apps = computers
            .find {
                val a = it.uuid == uuid &&
                        it.state == ComputerDetails.State.ONLINE &&
                        it.pairState == PairingManager.PairState.PAIRED
                KLog.common.d("test: uuid=${it.uuid} app=${it.rawAppList} $a")
                a
            }?.let { details ->
                KLog.common.d("targetCom=${details.rawAppList}")
                CacheHelper.readInputStreamToString(
                    CacheHelper.openCacheFileForInput(
                        context.cacheDir, "applist", uuid
                    )
                )?.let { NvHTTP.getAppListByReader(StringReader(it)) as List<NvApp> }
                    ?.map { GameApp(details, it) }
            }

        KLog.common.d("apps=${apps?.joinToString { it.toString() }}")
        return apps

    }

    override fun onCleared() {
        computerList.clear()
        super.onCleared()
    }
}