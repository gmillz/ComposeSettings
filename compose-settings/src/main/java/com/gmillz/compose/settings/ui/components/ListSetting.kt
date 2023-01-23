package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.SettingController

@Composable
fun <T> ListSetting(
    controller: SettingController<T>,
    entries: List<ListEntry<T>>,
    label: String,
    enabled: Boolean = true,
    description: String? = null
) {
    ListSetting(
        entries = entries,
        value = controller.state.value,
        onValueChange = controller::onChange,
        label = label,
        enabled = enabled,
        description = description
    )
}

@Composable
fun <T> ListSetting(
    entries: List<ListEntry<T>>,
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    enabled: Boolean = true,
    description: String? = null
) {
    val bottomSheetHandler = bottomSheetHandler
    val currentDescription = description ?: entries.firstOrNull { it.value == value }
        ?.label?.invoke()

    SettingTemplate(
        title = label,
        description = currentDescription,
        enabled = enabled,
        onClick = {
            bottomSheetHandler.show {
                AlertBottomSheetContent(
                    title = { Text(text = label) },
                    buttons = {
                        OutlinedButton(onClick = { bottomSheetHandler.hide() }) {
                            Text(text = stringResource(id = android.R.string.cancel))
                        }
                    }
                ) {
                    LazyColumn {
                        itemsIndexed(entries) { index, item ->
                            if (index > 0) {
                                SettingDivider(startIndent = 40.dp)
                            }
                            SettingTemplate(
                                title = item.label(),
                                enabled = item.enabled,
                                startWidget = {
                                    item.icon?.invoke()
                                },
                                endWidget = {
                                    RadioButton(
                                        selected = item.value == value,
                                        onClick = null,
                                        enabled = item.enabled
                                    )
                                },
                                onClick = {
                                    onValueChange(item.value)
                                    bottomSheetHandler.hide()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

class ListEntry<T>(
    val value: T,
    val enabled: Boolean = true,
    val label: @Composable () -> String,
    val icon: (@Composable () -> Unit)? = null,
)
