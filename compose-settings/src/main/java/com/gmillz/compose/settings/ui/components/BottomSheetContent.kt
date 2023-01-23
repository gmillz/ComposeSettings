package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val emptyBottomSheetContent = BottomSheetContent {
    Spacer(modifier = Modifier.height(1.dp))
}

data class BottomSheetContent(
    val content: @Composable () -> Unit
)
