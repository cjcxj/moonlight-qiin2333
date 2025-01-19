package com.model

import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.NvApp

data class GameApp(val computer: ComputerDetails, val nvApp: NvApp) {
    companion object {
        @JvmStatic
        val EMPTY = GameApp(ComputerDetails(), NvApp())
    }
}
