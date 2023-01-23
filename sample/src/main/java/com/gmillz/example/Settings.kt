package com.gmillz.example

import android.content.Context
import com.gmillz.compose.settings.BaseSettings

class Settings(context: Context): BaseSettings(context) {

    val testSwitch1 = BooleanSetting("test_switch_1", false)
    val test2 = IntSetting("test_int_setting_1", 0)
}