package com.gmillz.compose.settings.extensions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.navigationBarsOrDisplayCutoutPadding(): Modifier = composed {
    val sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    val navigationBars = WindowInsets.navigationBars.only(sides).asPaddingValues()
    val displayCutout = WindowInsets.displayCutout.only(sides).asPaddingValues()
    padding(max(navigationBars, displayCutout))
}

inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

inline fun <T> Modifier.addIfNotNull(
    value: T?,
    crossinline factory: Modifier.(T) -> Modifier
): Modifier =
    if (value != null) factory(value) else this
