package com.gmillz.example.ui.screens

import androidx.compose.runtime.Composable
import com.gmillz.compose.settings.getController
import com.gmillz.compose.settings.ui.SettingsScreen
import com.gmillz.compose.settings.ui.components.ListEntry
import com.gmillz.compose.settings.ui.components.ListSetting
import com.gmillz.compose.settings.ui.components.SettingSwitch
import com.gmillz.compose.settings.ui.components.SettingTemplate
import com.gmillz.compose.settings.ui.components.SettingsPage
import com.gmillz.compose.settings.util.LocalNavController
import com.gmillz.example.CustomClass
import com.gmillz.example.Settings

@Composable
fun TopLevelSettings(
    settings: Settings
) {
    SettingsPage {
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
        ListSetting(
            controller = settings.test3.getController(),
            entries = listOf(
                ListEntry(CustomClass("name1", "label1"), true, { "name1" }),
                ListEntry(CustomClass("name2", "label2"), true, { "name2" })
            ),
            label = "Sample CustomClass ListSetting"
        )
    }
}