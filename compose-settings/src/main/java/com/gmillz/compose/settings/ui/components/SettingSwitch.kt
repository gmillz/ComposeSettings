package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.SettingController

@Composable
fun SettingSwitch(
    controller: SettingController<Boolean>,
    label: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val checked = controller.state.value
    SettingSwitch(
        checked = checked,
        onCheckedChange = controller::onChange,
        label = label,
        description = description,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun SettingSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    SettingTemplate(
        modifier = Modifier.clickable(enabled = enabled) {
            if (onClick != null) {
                onClick()
            } else {
                onCheckedChange(!checked)
            }
        },
        contentModifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp)
            .padding(start = 16.dp),
        title =  label,
        description = description,
        endWidget = {
            if (onClick != null) {
                Spacer(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Switch(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .height(24.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        enabled = enabled,
        applyPaddings = false
    )
}
