package com.gmillz.compose.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

interface SettingController<T> {
    val state: State<T>
    fun onChange(newValue: T)

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value
    operator fun setValue(thisObj: Any?, property: KProperty<*>, newValue: T) = onChange(newValue)
}

private class MutableStateSettingController<T>(
    private val mutableState: MutableState<T>
) : SettingController<T> {
    override val state = mutableState

    override fun onChange(newValue: T) {
        mutableState.value = newValue
    }
}

class SettingControllerImpl<T>(
    private val get: () -> T,
    private val set: (T) -> Unit
) : SettingController<T>, SettingChangeListener<T> {
    private val stateInternal = mutableStateOf(get())
    override val state: State<T> get() = stateInternal

    override fun onChange(newValue: T) {
        set(newValue)
        stateInternal.value = newValue
    }

    override fun onSettingChange(value: T) {
        stateInternal.value = get()
    }
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
fun <T> SettingEntry<T>.getController() = getController(this, ::get, ::set)

@Composable
fun <T> SettingEntry<T>.getState() = getController().state

@Composable
fun <T> SettingEntry<T>.observeAsState() = getController().state

fun <T> SettingEntry<T>.asFlow(): Flow<T> {
    return callbackFlow {
        val listener = SettingChangeListener<T> {
            trySend(it)
        }
        addListener(listener)
        awaitClose { removeListener(listener) }
    }
}

@Composable
private fun <P> getController(
    setting: SettingEntry<P>,
    get: () -> P,
    set: (P) -> Unit
): SettingController<P> {
    val controller = remember { SettingControllerImpl(get, set) }
    DisposableEffect(setting) {
        setting.addListener(controller)
        onDispose { setting.removeListener(controller) }
    }
    return controller
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

@Composable
fun <T> MutableState<T>.asSettingController(): SettingController<T> {
    return remember(this) { MutableStateSettingController(this) }
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
fun <T> customSettingController(value: T, onValueChange: (T) -> Unit): SettingController<T> {
    val state = remember {
        mutableStateOf(value)
    }
    state.value = value
    return object : SettingController<T> {
        override val state = state
        override fun onChange(newValue: T) {
            onValueChange(newValue)
        }
    }
}

@Composable
operator fun SettingController<Boolean>.not(): SettingController<Boolean> {
    return rememberTransformController(
        controller = this,
        transformGet = { !it },
        transformSet = { !it })
}
