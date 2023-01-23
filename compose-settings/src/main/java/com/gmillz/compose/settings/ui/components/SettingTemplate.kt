package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.extensions.addIf

@Composable
fun SettingTemplate(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: @Composable () -> Unit = { },
    enabled: Boolean = true,
    applyPaddings: Boolean = true,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: () -> Unit = {}
) {
    SettingTemplate(
        modifier = modifier,
        contentModifier = contentModifier,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Normal
            )
        },
        description = {
            description?.let {
                Text(
                    text = description,
                    fontWeight = FontWeight.Light
                )
            }
        },
        startWidget = startWidget,
        endWidget = endWidget,
        enabled = enabled,
        applyPaddings = applyPaddings,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        verticalAlignment = verticalAlignment,
        onClick = onClick
    )
}

@Composable
fun SettingTemplate(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    applyPaddings: Boolean = true,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: () -> Unit = {}
) {
    val contentAlphaDisabled = 0.38f
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = verticalAlignment,
            modifier = modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .addIf(applyPaddings) {
                    padding(horizontal = horizontalPadding, vertical = verticalPadding)
                }
        ) {
            startWidget?.let {
                startWidget()
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
            }
            Row(
                modifier = contentModifier
                    .weight(1f)
                    .addIf(!enabled) {
                        alpha(contentAlphaDisabled)
                    },
                verticalAlignment = verticalAlignment
            ) {
                Column(Modifier.weight(1f)) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                        LocalTextStyle provides MaterialTheme.typography.bodyLarge
                    ) {
                        title()

                    }
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    ) {
                        description()
                    }
                }
            }
            endWidget?.let {
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
                endWidget()
            }
        }
    }
}
