package com.gmillz.example.ui.screens

import androidx.compose.runtime.Composable
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.ListEntry
import com.gmillz.compose.settings.ui.components.ListSetting
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.util.LocalNavController
import com.gmillz.example.Settings

@Composable
fun TopLevelSettings(
    settings: Settings
) {
    SettingsScreen(navController = LocalNavController.current) {
        SettingTemplate(title = "Test Template")
        SettingSwitch(
            controller = settings.testSwitch1.getController(),
            label = "Test Switch 1"
        )
        ListSetting(
            controller = settings.test2.getController(),
            entries = listOf(
                ListEntry(0, true, { "Entry 1" }),
                ListEntry(1, true, { "Entry 2" })
            ),
            label = "Sample List Setting"
        )
    }
}