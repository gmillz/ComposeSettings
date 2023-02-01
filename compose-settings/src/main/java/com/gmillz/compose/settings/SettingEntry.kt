package com.gmillz.compose.settings

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty

sealed interface SettingEntry<T, K> {
    val key: Preferences.Key<K>
    val defaultValue: T

    fun get(): Flow<T>
    fun set(value: T)

    suspend fun first() = get().first()

    fun firstBlocking() = runBlocking { first() }

    fun setBlocking(value: T) {
        runBlocking { set(value) }
    }

    fun onEach(
        launchIn: CoroutineScope,
        block: (T) -> Unit
    ) {
        get().onEach { block(it) }.launchIn(launchIn)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): Flow<T> = get()
    operator fun setValue(thisObj: Any?, property: KProperty<*>, newValue: T) = set(newValue)
}
