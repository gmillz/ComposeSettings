package com.gmillz.compose.settings.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gmillz.compose.settings.SettingController

@Composable
fun TextSetting(
    controller: SettingController<String>,
    label: String,
    description: (String) -> String? = { it },
    enabled: Boolean = true
) {
    val value = controller.state.value
    TextSetting(
        value = value,
        onChange = controller::onChange,
        label = label,
        description = description,
        enabled = enabled
    )
}

@Composable
fun TextSetting(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    description: (String) -> String? = { it },
    enabled: Boolean = true
) {
    val bottomSheetHandler = bottomSheetHandler
    SettingTemplate(
        title = label,
        description = description(value),
        enabled = enabled,
        onClick = {
            bottomSheetHandler.show {
                TextSettingDialog(
                    title = label,
                    initialValue = value,
                    onDismissRequest = { bottomSheetHandler.hide() },
                    onConfirm = onChange
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextSettingDialog(
    title: String,
    initialValue: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertBottomSheetContent(
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        buttons = {
            OutlinedButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            Button(
                onClick = {
                    onDismissRequest()
                    onConfirm(value)
                }
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}