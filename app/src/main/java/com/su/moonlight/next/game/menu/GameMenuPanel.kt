package com.su.moonlight.next.game.menu

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.KeyboardAlt
import androidx.compose.material.icons.rounded.KeyboardCommandKey
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material.icons.rounded.TouchApp
import com.limelight.Game
import com.limelight.binding.input.GameInputDevice
import com.limelight.binding.input.KeyboardTranslator
import com.limelight.nvstream.NvConnection
import com.limelight.nvstream.input.KeyboardPacket
import com.limelight.preferences.PreferenceConfiguration
import com.su.moonlight.next.R
import com.su.moonlight.next.game.menu.options.CancelMenuOption
import com.su.moonlight.next.game.menu.options.DisconnectMenuOption
import com.su.moonlight.next.game.menu.options.KeyboardMenuOption
import com.su.moonlight.next.game.menu.options.MenuOption
import com.su.moonlight.next.game.menu.options.MenuPanelOption
import com.su.moonlight.next.game.menu.options.PanZoomModeMenuOption
import com.su.moonlight.next.game.menu.options.PerformanceOverlayMenuOption
import com.su.moonlight.next.game.menu.options.QuitSessionMenuOption
import com.su.moonlight.next.game.menu.options.SpecialButtonMenuOption
import org.json.JSONObject

class GameMenuPanel(
    private val game: Game,
    private val conn: NvConnection,
    private val device: GameInputDevice?
) {

    init {
        showMenu()
    }

    private fun getString(id: Int): String {
        return game.resources.getString(id)
    }

    private fun sendKeys(keys: ShortArray) {
        val modifier = byteArrayOf(0.toByte())

        for (key in keys) {
            conn.sendKeyboardInput(key, KeyboardPacket.KEY_DOWN, modifier[0], 0.toByte())

            // Apply the modifier of the pressed key, e.g. CTRL first issues a CTRL event (without
            // modifier) and then sends the following keys with the CTRL modifier applied
            modifier[0] = (modifier[0].toInt() or getModifier(key).toInt()).toByte()
        }

        Handler(Looper.getMainLooper()).postDelayed((Runnable {
            for (pos in keys.indices.reversed()) {
                val key = keys[pos]

                // Remove the keys modifier before releasing the key
                modifier[0] = (modifier[0].toInt() and getModifier(key).toInt().inv()).toByte()

                conn.sendKeyboardInput(key, KeyboardPacket.KEY_UP, modifier[0], 0.toByte())
            }
        }), KEY_UP_DELAY)
    }

    private fun runWithGameFocus(runnable: Runnable?) {
        // Ensure that the Game activity is still active (not finished)
        if (game.isFinishing || runnable == null) {
            return
        }
        // Check if the game window has focus again, if not try again after delay
        if (!game.hasWindowFocus()) {
            Handler().postDelayed({ runWithGameFocus(runnable) }, TEST_GAME_FOCUS_DELAY)
            return
        }
        // Game Activity has focus, run runnable
        runnable.run()
    }

    private fun showMenuDialog(title: String, options: List<MenuPanelOption>) {
        showMenuPanelDialog(game, title, options, ::runWithGameFocus)
    }

    private fun showSpecialKeysMenu() {
        val options: MutableList<MenuPanelOption> = ArrayList()

        SpecialButtonMenuOption.init()

        if (!PreferenceConfiguration.readPreferences(game).enableClearDefaultSpecial) {
            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_esc)
            ) { sendKeys(shortArrayOf(KeyboardTranslator.VK_ESCAPE.toShort())) })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_f11)
            ) { sendKeys(shortArrayOf(KeyboardTranslator.VK_F11.toShort())) })

            options.add(SpecialButtonMenuOption(getString(R.string.game_menu_send_keys_f12)
            ) { sendKeys(shortArrayOf(KeyboardTranslator.VK_F12.toShort())) })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_alt_f4)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_F4.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_alt_enter)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_RETURN.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_ctrl_v)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LCONTROL.toShort(),
                        KeyboardTranslator.VK_V.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_win)
            ) { sendKeys(shortArrayOf(KeyboardTranslator.VK_LWIN.toShort())) })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_win_d)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_D.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_win_g)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_G.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_ctrl_alt_tab)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LCONTROL.toShort(),
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_TAB.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_shift_tab)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LSHIFT.toShort(),
                        KeyboardTranslator.VK_TAB.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_win_shift_left)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_LSHIFT.toShort(),
                        KeyboardTranslator.VK_LEFT.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_ctrl_alt_shift_q)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LCONTROL.toShort(),
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_LSHIFT.toShort(),
                        KeyboardTranslator.VK_Q.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_ctrl_alt_shift_f1)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LCONTROL.toShort(),
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_LSHIFT.toShort(),
                        KeyboardTranslator.VK_F1.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_ctrl_alt_shift_f12)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LCONTROL.toShort(),
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_LSHIFT.toShort(),
                        KeyboardTranslator.VK_F12.toShort()
                    )
                )
            })

            options.add(SpecialButtonMenuOption(
                getString(R.string.game_menu_send_keys_alt_b)
            ) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_LMENU.toShort(),
                        KeyboardTranslator.VK_B.toShort()
                    )
                )
            })
            options.add(SpecialButtonMenuOption(getString(R.string.game_menu_send_keys_win_x_u_s)) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_X.toShort()
                    )
                )
                Handler().postDelayed((Runnable {
                    sendKeys(
                        shortArrayOf(
                            KeyboardTranslator.VK_U.toShort(),
                            KeyboardTranslator.VK_S.toShort()
                        )
                    )
                }), 200)
            })
            options.add(SpecialButtonMenuOption(getString(R.string.game_menu_send_keys_win_x_u_u)) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_X.toShort()
                    )
                )
                Handler().postDelayed((Runnable {
                    sendKeys(
                        shortArrayOf(
                            KeyboardTranslator.VK_U.toShort(),
                            KeyboardTranslator.VK_U.toShort()
                        )
                    )
                }), 200)
            })
            options.add(SpecialButtonMenuOption(getString(R.string.game_menu_send_keys_win_x_u_r)) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_X.toShort()
                    )
                )
                Handler().postDelayed((Runnable {
                    sendKeys(
                        shortArrayOf(
                            KeyboardTranslator.VK_U.toShort(),
                            KeyboardTranslator.VK_R.toShort()
                        )
                    )
                }), 200)
            })
            options.add(SpecialButtonMenuOption(getString(R.string.game_menu_send_keys_win_x_u_i)) {
                sendKeys(
                    shortArrayOf(
                        KeyboardTranslator.VK_LWIN.toShort(),
                        KeyboardTranslator.VK_X.toShort()
                    )
                )
                Handler().postDelayed((Runnable {
                    sendKeys(
                        shortArrayOf(
                            KeyboardTranslator.VK_U.toShort(),
                            KeyboardTranslator.VK_I.toShort()
                        )
                    )
                }), 200)
            })
        }

        //自定义导入的指令
        val preferences = game.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE)
        val value = preferences.getString(KEY_NAME, "")

        if (!TextUtils.isEmpty(value)) {
            try {
                val `object` = JSONObject(value)
                val array = `object`.optJSONArray("data")
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()) {
                        val object1 = array.getJSONObject(i)
                        val name = object1.optString("name")
                        val array1 = object1.getJSONArray("data")
                        val datas = ShortArray(array1.length())
                        for (j in 0 until array1.length()) {
                            val code = array1.getString(j)
                            datas[j] = code.substring(2).toInt(16).toShort()
                        }
                        val option = MenuOption(name) { sendKeys(datas) }
                        options.add(option)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(game, getString(R.string.wrong_import_format), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        options.add(CancelMenuOption())

        showMenuDialog(getString(R.string.game_menu_send_keys), options)
    }

    private fun showAdvancedMenu() {
        val options = mutableListOf<MenuOption>()

        options.add(MenuOption(
            getString(R.string.game_menu_toggle_keyboard_model),
            true,
            Icons.Rounded.KeyboardCommandKey
        ) { game.showHideKeyboardController() })

        options.add(MenuOption(
            getString(R.string.game_menu_toggle_virtual_model), true, Icons.Rounded.Games
        ) { game.showHideVirtualController() })
        options.add(MenuOption(
            getString(R.string.game_menu_toggle_virtual_keyboard_model),
            true,
            Icons.Rounded.KeyboardAlt
        ) { game.showHidekeyBoardLayoutController() })

        options.add(MenuOption(
            getString(R.string.game_menu_task_manager), true, Icons.Rounded.Task
        ) {
            sendKeys(
                shortArrayOf(
                    KeyboardTranslator.VK_LCONTROL.toShort(),
                    KeyboardTranslator.VK_LSHIFT.toShort(),
                    KeyboardTranslator.VK_ESCAPE.toShort()
                )
            )
        })

        options.add(MenuOption(
            getString(R.string.game_menu_switch_touch_sensitivity_model),
            true,
            Icons.Rounded.TouchApp
        ) { game.switchTouchSensitivity() })

        if (device != null) {
            device.gameMenuOptions.forEach { options.add(it) }
        }

        options.add(CancelMenuOption())

        showMenuDialog(getString(R.string.game_menu_advanced), options)
    }

    private fun showMenu() {
        val options: MutableList<MenuPanelOption> = ArrayList()

        options.add(QuitSessionMenuOption(game))
        options.add(DisconnectMenuOption(game))

        options.add(KeyboardMenuOption(game))



        options.add(MenuOption(
            getString(R.string.game_menu_rotate_screen), true, Icons.Rounded.Autorenew,
        ) { game.rotateScreen() })

        options.add(PanZoomModeMenuOption(game))

        options.add(MenuOption(
            getString(R.string.game_menu_advanced), true, Icons.Rounded.Apps,
        ) { this.showAdvancedMenu() })

        options.add(
            MenuOption(
                getString(R.string.game_menu_send_keys),
                false,
                Icons.Default.Computer
            ) { this.showSpecialKeysMenu() })

        options.add(PerformanceOverlayMenuOption(game))

        if (game.presentation == null) {
            options.add(MenuOption(
                getString(R.string.game_menu_select_mouse_mode), true, Icons.Rounded.Mouse
            ) { game.selectMouseModeModal() })
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            options.add(
                MenuOption(
                    getString(R.string.screenshot),
                    false,
                    Icons.Default.Screenshot
                ) { game.preScreenshot() })
        }

        options.add(CancelMenuOption())

        showMenuDialog(getString(R.string.quick_menu_title), options)
    }

    companion object {
        private const val TEST_GAME_FOCUS_DELAY: Long = 10
        private const val KEY_UP_DELAY: Long = 25

        const val PREF_NAME: String = "specialPrefs" // SharedPreferences的名称

        const val KEY_NAME: String = "special_key" // 要保存的键名称

        private fun getModifier(key: Short): Byte {
            return when (key.toInt()) {
                KeyboardTranslator.VK_LSHIFT -> KeyboardPacket.MODIFIER_SHIFT
                KeyboardTranslator.VK_LCONTROL -> KeyboardPacket.MODIFIER_CTRL
                KeyboardTranslator.VK_LWIN -> KeyboardPacket.MODIFIER_META
                KeyboardTranslator.VK_LMENU -> KeyboardPacket.MODIFIER_ALT
                else -> 0
            }
        }
    }
}