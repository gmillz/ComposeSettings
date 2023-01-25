package com.gmillz.compose.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArraySet

@Suppress("LeakingThis")
open class BaseSettings(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val settingMap = hashMapOf<String, BaseSetting<*>>()

    private var changedPrefs: MutableSet<BaseSetting<*>>? = null

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val pref = settingMap[key] ?: return
        val changedSet = changedPrefs
        if (changedSet != null) {
            changedSet.add(pref)
        } else {
            pref.invalidate()
            pref.onSharedPreferenceChange()
        }
    }

    private inline fun edit(crossinline block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit { block(this) }
    }

    abstract inner class BaseSetting<T>(
        override val key: String,
        private val primaryListener: ChangeListener?
    ) : SettingEntry<T> {
        protected var loaded = false
        private val listeners = CopyOnWriteArraySet<SettingChangeListener<T>>()

        fun invalidate() {
            loaded = false
        }

        fun onSharedPreferenceChange() {
            loaded = false
            primaryListener?.invoke()
            listeners.forEach { listener ->
                listener.onSettingChange(get())
            }
        }

        override fun addListener(listener: SettingChangeListener<T>) {
            listeners.add(listener)
            listener.onSettingChange(get())
        }

        override fun removeListener(listener: SettingChangeListener<T>) {
            listeners.remove(listener)
        }
    }

    abstract inner class StringBasedSetting<T>(
        key: String,
        override val defaultValue: T,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<T>(key, primaryListener) {
        private var currentValue: T? = null

        init {
            settingMap[key] = this
        }

        override fun get(): T {
            if (!loaded) {
                currentValue = if (prefs.contains(key)) {
                    parse(prefs.getString(key, null)!!)
                } else {
                    defaultValue
                }
                loaded = true
            }
            return currentValue ?: defaultValue
        }

        override fun set(newValue: T) {
            currentValue = newValue
            edit { putString(key, stringify(newValue)) }
        }

        protected abstract fun parse(stringValue: String): T
        protected abstract fun stringify(value: T): String
    }

    inner class StringSetting(
        key: String,
        defaultValue: String,
        primaryListener: ChangeListener? = null
    ) : StringBasedSetting<String>(key, defaultValue, primaryListener) {
        override fun parse(stringValue: String) = stringValue
        override fun stringify(value: String) = value
    }

    inner class BooleanSetting(
        key: String,
        override val defaultValue: Boolean,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<Boolean>(key, primaryListener) {
        private var currentValue = false

        init {
            settingMap[key] = this
        }

        override fun get(): Boolean {
            if (!loaded) {
                currentValue = prefs.getBoolean(key, defaultValue)
                loaded = true
            }
            return currentValue
        }

        override fun set(newValue: Boolean) {
            currentValue = newValue
            edit { putBoolean(key, newValue) }
        }
    }

    open inner class IntSetting(
        key: String,
        private val defaultValueInternal: Int,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<Int>(key, primaryListener) {
        override val defaultValue = defaultValueInternal
        private var currentValue = 0

        init {
            settingMap[key] = this
        }

        override fun get(): Int {
            if (!loaded) {
                currentValue = try {
                    prefs.getInt(key, defaultValueInternal)
                } catch (_: ClassCastException) {
                    prefs.getFloat(key, defaultValueInternal.toFloat()).toInt()
                }
                loaded = true
            }
            return currentValue
        }

        override fun set(newValue: Int) {
            currentValue = newValue
            edit { putInt(key, newValue) }
        }
    }

    inner class FloatSetting(
        key: String,
        override val defaultValue: Float,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<Float>(key, primaryListener) {
        private var currentValue = 0f

        init {
            settingMap[key] = this
        }

        override fun get(): Float {
            if (!loaded) {
                currentValue = prefs.getFloat(key, defaultValue)
                loaded = true
            }
            return currentValue
        }

        override fun set(newValue: Float) {
            currentValue = newValue
            edit { putFloat(key, newValue) }
        }
    }

    inner class StringSetSetting(
        key: String,
        override val defaultValue: Set<String>,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<Set<String>>(key, primaryListener) {
        private var currentValue = setOf<String>()

        init {
            settingMap[key] = this
        }

        override fun get(): Set<String> {
            if (!loaded) {
                currentValue = prefs.getStringSet(key, defaultValue)!!
                loaded = true
            }
            return currentValue
        }

        override fun set(newValue: Set<String>) {
            currentValue = newValue
            edit { putStringSet(key, newValue) }
        }
    }

    open inner class ObjectSetting<T>(
        key: String,
        defaultValue: T,
        private val parseFunc: (stringValue: String) -> T,
        private val stringifyFunc: (value: T) -> String,
        primaryListener: ChangeListener? = null
    ) : StringBasedSetting<T>(key, defaultValue, primaryListener) {
        override fun parse(stringValue: String) = parseFunc(stringValue)
        override fun stringify(value: T) = stringifyFunc(value)
    }

    abstract inner class MutableMapSetting<K, V>(
        key: String,
        primaryListener: ChangeListener? = null
    ) : BaseSetting<Map<K, V>>(key, primaryListener) {

        override val defaultValue = mapOf<K, V>()
        private val valueMap = mutableMapOf<K, V>()

        init {
            val obj = JSONObject(prefs.getString(key, "{}")!!)
            obj.keys().forEach {
                valueMap[unflattenKey(it)] = unflattenValue(obj.getString(it))
            }
            settingMap[key] = this
        }

        override fun get() = HashMap(valueMap)

        override fun set(newValue: Map<K, V>) {
            throw NotImplementedError()
        }

        open fun flattenKey(key: K) = key.toString()
        abstract fun unflattenKey(key: String): K

        open fun flattenValue(value: V) = value.toString()
        abstract fun unflattenValue(value: String): V

        operator fun set(key: K, value: V?) {
            if (value != null) {
                valueMap[key] = value
            } else {
                valueMap.remove(key)
            }
            saveChanges()
        }

        private fun saveChanges() {
            val obj = JSONObject()
            valueMap.entries.forEach { obj.put(flattenKey(it.key), flattenValue(it.value)) }
            edit { putString(key, obj.toString()) }
        }

        operator fun get(key: K): V? {
            return valueMap[key]
        }

        fun clear() {
            valueMap.clear()
            saveChanges()
        }
    }

    /*inner class ColorSetting(
        key: String,
        override val defaultValue: ColorOption,
        primaryListener: ChangeListener?
    ): StringBasedSetting<ColorOption>(key, defaultValue, primaryListener) {

        override fun parse(stringValue: String) = ColorOption.fromString(stringValue)
        override fun stringify(value: ColorOption) = value.toString()
    }*/
}
