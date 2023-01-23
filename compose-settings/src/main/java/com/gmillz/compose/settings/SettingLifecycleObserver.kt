package com.gmillz.compose.settings

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SettingLifecycleObserver<T>(
    private val setting: SettingEntry<T>,
    private val onChange: Runnable
) : DefaultLifecycleObserver, SettingChangeListener {

    fun connectListener() {
        setting.addListener(this)
    }

    fun disconnectListener() {
        setting.removeListener(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        connectListener()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        disconnectListener()
    }

    override fun onSettingChange() {
        onChange.run()
    }
}
