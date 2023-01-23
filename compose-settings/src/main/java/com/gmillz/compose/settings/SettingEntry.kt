package com.gmillz.compose.settings

import android.view.View
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import com.gmillz.compose.settings.util.SafeCloseable
import kotlin.reflect.KProperty

typealias ChangeListener = () -> Unit

sealed interface SettingEntry<T> {
    val key: String
    val defaultValue: T

    fun get(): T
    fun set(newValue: T)

    fun addListener(listener: SettingChangeListener)
    fun removeListener(listener: SettingChangeListener)

    fun subscribeChanges(onChange: Runnable): SafeCloseable {
        val observer = SettingLifecycleObserver(this, onChange)
        observer.connectListener()
        return SafeCloseable { observer.disconnectListener() }
    }

    fun subscribeChanges(lifecycleOwner: LifecycleOwner, onChange: Runnable) {
        lifecycleOwner.lifecycle.addObserver(SettingLifecycleObserver(this, onChange))
    }

    fun subscribeChanges(view: View, onChange: Runnable) {
        val observer = SettingLifecycleObserver(this, onChange)
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                observer.connectListener()
            }

            override fun onViewDetachedFromWindow(v: View) {
                observer.disconnectListener()
            }
        })
    }

    fun subscribeValues(lifecycleOwner: LifecycleOwner, onChange: Consumer<T>) {
        onChange.accept(get())
        subscribeChanges(lifecycleOwner) {
            onChange.accept(get())
        }
    }

    fun subscribeValues(view: View, onChange: Consumer<T>) {
        onChange.accept(get())
        subscribeChanges(view) {
            onChange.accept(get())
        }
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = get()
    operator fun setValue(thisObj: Any?, property: KProperty<*>, newValue: T) = set(newValue)
}
