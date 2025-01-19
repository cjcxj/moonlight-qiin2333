package com.su.moonlight.next.img

import coil3.key.Keyer
import coil3.request.Options
import com.model.GameApp

class AppKeyer : Keyer<GameApp> {
    override fun key(data: GameApp, options: Options) =
        "game_app_icon_${data.computer.uuid}_${data.nvApp.appId}"
}