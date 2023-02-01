package com.gmillz.compose.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.settingsDataStore by preferencesDataStore("settings")

open class BaseSettings(context: Context) {

    private val dataStore = context.settingsDataStore

    fun <T> setting(
        key: Preferences.Key<T>,
        defaultValue: T,
        onSet: (T) -> Unit = {}
    ): Setting<T, T> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            onSet = onSet,
            parse = { it },
            save = { it },
            settingsDataStore = dataStore
        )
    }

    fun <T, K> setting(
        key: Preferences.Key<K>,
        defaultValue: T,
        onSet: (T) -> Unit = {},
        parse: (K) -> T,
        save: (T) -> K,
    ): SettingEntry<T, K> {
        return Setting(
            parse = parse,
            save = save,
            onSet = onSet,
            defaultValue = defaultValue,
            key = key,
            settingsDataStore = dataStore

        )
    }

    inner class Setting<T, K>(
        val parse: (K) -> T,
        val save: (T) -> K,
        val onSet: (T) -> Unit,
        override val defaultValue: T,
        override val key: Preferences.Key<K>,
        val settingsDataStore: DataStore<Preferences>
    ): SettingEntry<T, K> {

        private fun K?.parsedOrDefault() = this?.let { parse(it) } ?: defaultValue
        override fun get(): Flow<T> {
            return settingsDataStore.data.map {preferences ->
                preferences[key].parsedOrDefault()
            }
        }

        override fun set(value: T) {
            runBlocking {
                settingsDataStore.edit { prefs ->
                    prefs[key] = save(value)
                }
                onSet(value)
            }
        }

    }
}
