package com.gmillz.compose.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

interface SettingController<T> {
    val state: State<T>
    fun onChange(newValue: T)

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value
    operator fun setValue(thisObj: Any?, property: KProperty<*>, newValue: T) = onChange(newValue)
}

private class StateSettingController<T>(
    override val state: State<T>,
    private val set: (T) -> Unit
) : SettingController<T> {
    override fun onChange(newValue: T) {
        set(newValue)
    }
}

@Composable
fun <T, K> SettingEntry<T, K>.asState(): State<T> {
    return this.get().collectAsState(initial = defaultValue)
}

@Composable
fun <T, K> SettingEntry<T, K>.getController(): SettingController<T> {
    return createStateController(state = asState(), set = this::set)
}

@Composable
private fun <T> createStateController(
    state: State<T>,
    set: suspend (T) -> Unit
): SettingController<T> {
    val scope = rememberCoroutineScope()
    return remember {
        StateSettingController(state) {
            scope.launch { set(it) }
        }
    }
}

@Composable
fun <T, R> rememberTransformController(
    controller: SettingController<T>,
    transformGet: (T) -> R,
    transformSet: (R) -> T
): SettingController<R> = remember(controller) {
    TransformSettingController(controller, transformGet, transformSet)
}

private class TransformSettingController<T, R>(
    private val parent: SettingController<T>,
    private val transformGet: (T) -> R,
    private val transformSet: (R) -> T
) : SettingController<R> {
    override val state = derivedStateOf { transformGet(parent.state.value) }
    override fun onChange(newValue: R) {
        parent.onChange(transformSet(newValue))
    }
}

@Composable
operator fun SettingController<Boolean>.not(): SettingController<Boolean> {
    return rememberTransformController(
        controller = this,
        transformGet = { !it },
        transformSet = { !it })
}
