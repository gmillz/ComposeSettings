package com.gmillz.compose.settings.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.ui.components.bottomsheet.ModalBottomSheetLayout
import com.gmillz.compose.settings.ui.components.bottomsheet.ModalBottomSheetState
import com.gmillz.compose.settings.ui.components.bottomsheet.ModalBottomSheetValue
import kotlinx.coroutines.launch
import kotlin.math.max

internal val LocalBottomSheetHandler = staticCompositionLocalOf { BottomSheetHandler() }

val bottomSheetHandler: BottomSheetHandler
    @Composable
    @ReadOnlyComposable
    get() = LocalBottomSheetHandler.current

@Composable
fun ProvideBottomSheetHandler(
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var onDismiss by remember { mutableStateOf({}) }
    val bottomSheetState = remember {
        ModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmStateChange = {
                if (it == ModalBottomSheetValue.Hidden) onDismiss()
                true
            },
        )
    }
    var bottomSheetContent by remember { mutableStateOf(emptyBottomSheetContent) }
    val bottomSheetHandler = remember {
        BottomSheetHandler(
            show = { sheetContent ->
                bottomSheetContent = BottomSheetContent(content = sheetContent)
                if (bottomSheetState.isVisible.not()) coroutineScope.launch { bottomSheetState.show() }
            },
            hide = {
                onDismiss()
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            },
            onDismiss = {
                onDismiss = it
            },
        )
    }

    ModalBottomSheetLayout(
        sheetContent = {
            val isSheetShown = bottomSheetState.isAnimationRunning || bottomSheetState.isVisible
            BackHandler(enabled = isSheetShown) {
                bottomSheetHandler.hide()
            }
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                StatusBarOffset {
                    bottomSheetContent.content()
                }
            }
        },
        sheetState = bottomSheetState,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
    ) {
        CompositionLocalProvider(LocalBottomSheetHandler provides bottomSheetHandler) {
            content()
        }
    }
}

class BottomSheetHandler(
    val show: (@Composable () -> Unit) -> Unit = {},
    val hide: () -> Unit = {},
    val onDismiss: (() -> Unit) -> Unit = {},
)

@Composable
fun StatusBarOffset(content: @Composable () -> Unit) {
    val statusBar = WindowInsets.statusBars.getTop(LocalDensity.current)
    val displayCutout = WindowInsets.displayCutout.getTop(LocalDensity.current)
    val statusBarHeight = max(statusBar, displayCutout)
    val topOffset = statusBarHeight + with(LocalDensity.current) { 8.dp.roundToPx() }

    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val newConstraints = Constraints(
                    minWidth = constraints.minWidth,
                    maxWidth = constraints.maxWidth,
                    minHeight = constraints.minHeight,
                    maxHeight = when (constraints.maxHeight) {
                        Constraints.Infinity -> Constraints.Infinity
                        else -> constraints.maxHeight - topOffset
                    }
                )
                val placeable = measurable.measure(newConstraints)

                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    ) {
        content()
    }
}
