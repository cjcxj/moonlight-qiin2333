package com.su.moonlight.next.ui.game

import android.app.Activity
import androidx.collection.ArrayMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvApp
import com.su.moonlight.next.repository.ComputerDetailRepository
import com.su.moonlight.next.utils.KLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class GameViewModel : ViewModel() {

    private val kLog = KLog("GameViewModel").apply {
        printInRelease = false
    }
    private var gameList: ArrayMap<String, NvApp> = ArrayMap()

    @Composable
    fun collectComputerListState(uuid: String): State<List<NvApp>?> {
        val context = LocalContext.current as Activity
        val rawFlow by remember {
            mutableStateOf(
                ComputerDetailRepository.getComputerAppListFlow(context, uuid)
                    .map { app ->
                        app
                    }
                    .onEach {
                        kLog.d("update flow: ${it.hashCode()}")
                    }
                    .flowOn(Dispatchers.Default)
            )
        }

        return rawFlow.collectAsState(initial = emptyList<NvApp>())
    }


    override fun onCleared() {
        gameList.clear()
        super.onCleared()
    }
}