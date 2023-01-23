package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.R
import com.gmillz.compose.settings.SettingController
import com.gmillz.compose.settings.rememberTransformController
import kotlin.math.roundToInt

@Composable
fun SliderSetting(
    label: String,
    value: SettingController<Int>,
    valueRange: ClosedRange<Int>,
    step: Int,
    showAsPercentage: Boolean = false,
    onChange: (Int) -> Unit = {},
) {
    val transformState = rememberTransformController(
        controller = value,
        transformGet = { it.toFloat() },
        transformSet = { it.roundToInt() }
    )
    val start = valueRange.start.toFloat()
    val endInclusive = valueRange.endInclusive.toFloat()
    SliderSetting(
        label = label,
        value = transformState,
        valueRange = start..endInclusive,
        step = step.toFloat(),
        showAsPercentage = showAsPercentage,
        onChange = { onChange(it.roundToInt()) }
    )
}

@Composable
fun SliderSetting(
    label: String,
    value: SettingController<Float>,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    showAsPercentage: Boolean = false,
    onChange: (Float) -> Unit = {}
) {
    var sliderValue by remember { mutableStateOf(value.state.value) }

    SettingTemplate(
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Normal
                )
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground
                ) {
                    val lv = snapSliderValue(valueRange.start, sliderValue, step)
                    Text(
                        text = if (showAsPercentage) {
                            stringResource(
                                id = R.string.n_percent,
                                (lv * 100).roundToInt()
                            )
                        } else {
                            lv.roundToInt().toString()
                        },
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        },
        description = {
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    value.onChange(sliderValue)
                    onChange(sliderValue)
                },
                valueRange = valueRange,
                steps = getSteps(valueRange, step),
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 12.dp)
                    .padding(horizontal = 10.dp)
                    .height(24.dp)
            )
        },
        applyPaddings = false
    )
}

fun getSteps(valueRange: ClosedFloatingPointRange<Float>, step: Float): Int {
    if (step == 0f) return 0
    val start = valueRange.start
    val end = valueRange.endInclusive
    val steps = ((end - start) / step).toInt()
    require(start + step * steps == end) {
        "value range must be a multiple of step"
    }
    return steps - 1
}

fun snapSliderValue(start: Float, value: Float, step: Float): Float {
    if (step == 0f) return value
    val distance = value - start
    val stepsFromStart = (distance / step).roundToInt()
    val snappedDistance = stepsFromStart * step
    return start + snappedDistance
}
