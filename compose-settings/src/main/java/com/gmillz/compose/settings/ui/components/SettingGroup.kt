package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SettingGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    showDividers: Boolean = true,
    dividerStartIndent: Dp = 0.dp,
    dividerEndIndent: Dp = 0.dp,
    dividersToSkip: Int = 0,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        SettingGroupHeader(title)
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp
        ) {
            if (showDividers) {
                DividerColumn(
                    startIndent = dividerStartIndent,
                    endIndent = dividerEndIndent,
                    content = content,
                    dividersToSkip = dividersToSkip
                )
            } else {
                Column {
                    content()
                }
            }
        }
        SettingGroupFooter(description)
    }
}

@Composable
fun SettingGroupHeader(title: String?) {
    if (title == null) {
        Spacer(Modifier.requiredHeight(8.dp))
        return
    }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingGroupFooter(
    description: String? = null
) {
    description?.let {
        ExpandAndShrink(visible = true) {
            Row(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
