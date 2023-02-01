package com.gmillz.example

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gmillz.compose.settings.BaseSettings

class Settings(context: Context): BaseSettings(context) {

    val testSwitch1 = setting(
        key = booleanPreferencesKey("test_switch_1"),
        defaultValue = false,
    )

    val test2 = setting(
        key = intPreferencesKey("test_int_setting_1"),
        defaultValue = 0
    )

    val test3 = setting(
        key = stringPreferencesKey("custom_class_test"),
        defaultValue = CustomClass("name1", "label1"),
        parse = { CustomClass.fromString(it) },
        save = { it.toString() }
    )
}