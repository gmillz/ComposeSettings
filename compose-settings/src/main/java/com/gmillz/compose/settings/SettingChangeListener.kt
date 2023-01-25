package com.gmillz.compose.settings

fun interface SettingChangeListener<T> {
    fun onSettingChange(value: T)
}
