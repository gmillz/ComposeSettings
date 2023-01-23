package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SettingDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp
) {
    Divider(
        modifier = modifier
            .padding(start = startIndent + 16.dp, end = endIndent + 16.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(32.dp)
    )
}
