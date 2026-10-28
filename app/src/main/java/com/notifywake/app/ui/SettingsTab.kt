@file:OptIn(ExperimentalMaterial3Api::class)

package com.notifywake.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifywake.app.data.FilterMode
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings

@Composable
fun SettingsTab(settings: WakeSettings, store: SettingsStore, onOpenPicker: () -> Unit) {
    TabScaffold(title = "Settings") {
        SettingsGroup(
            header = "Apps",
            footer = if (settings.filterMode == FilterMode.ALL) {
                "Filtering is off: every app's notifications wake the screen."
            } else null,
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("Which apps can wake the screen", style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    SegmentedButton(
                        selected = settings.filterMode == FilterMode.ALL,
                        onClick = { store.setFilterMode(FilterMode.ALL) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("All apps") }
                    SegmentedButton(
                        selected = settings.filterMode == FilterMode.SELECTED,
                        onClick = { store.setFilterMode(FilterMode.SELECTED) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("Selected apps") }
                }
            }
            if (settings.filterMode == FilterMode.SELECTED) {
                val count = settings.selectedApps.size
                ListItem(
                    headlineContent = { Text("Choose apps") },
                    supportingContent = {
                        Text(
                            if (count == 0) "None selected, so nothing will wake the screen"
                            else "$count app${if (count == 1) "" else "s"} selected",
                            color = if (count == 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = transparentListItemColors(),
                    modifier = Modifier.clickable(onClick = onOpenPicker),
                )
            }
        }

        SettingsGroup(header = "Timing") {
            ChipChoice(
                label = "Cooldown between wakes",
                description = "Stops a burst of messages from turning the screen on again and again.",
                options = listOf(0 to "Off", 5 to "5 s", 10 to "10 s", 30 to "30 s", 60 to "1 min", 300 to "5 min"),
                selected = settings.cooldownSeconds,
                onSelect = store::setCooldownSeconds,
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            ChipChoice(
                label = "Keep screen on for at least",
                description = "After this, your normal lock-screen timeout applies.",
                options = listOf(3 to "3 s", 5 to "5 s", 8 to "8 s", 15 to "15 s"),
                selected = settings.screenOnSeconds,
                onSelect = store::setScreenOnSeconds,
            )
        }

        SettingsGroup(header = "Filters") {
            SwitchListItem(
                title = "Ignore silent notifications",
                description = "Skip low-priority notifications that don't sound or pop up",
                icon = Icons.Outlined.Notifications,
                checked = settings.ignoreSilent,
                onCheckedChange = store::setIgnoreSilent,
            )
            SwitchListItem(
                title = "Ignore ongoing notifications",
                description = "Music, downloads, navigation, \"app is running\"",
                icon = Icons.Outlined.PlayArrow,
                checked = settings.ignoreOngoing,
                onCheckedChange = store::setIgnoreOngoing,
            )
            SwitchListItem(
                title = "Respect Do Not Disturb",
                description = "Stay dark for notifications DND or Sleep mode silences",
                icon = Icons.Outlined.Face,
                checked = settings.respectDnd,
                onCheckedChange = store::setRespectDnd,
            )
        }

        SettingsGroup(
            header = "Wake method",
            footer = "The screen never wakes during a phone call. The phone always stays locked: " +
                "your PIN, password and fingerprint are still required.",
        ) {
            SwitchListItem(
                title = "Backup wake method",
                description = "If the screen is still off after a moment, wake it with a silent full-screen alert",
                icon = Icons.Outlined.Refresh,
                checked = settings.backupWake,
                onCheckedChange = store::setBackupWake,
            )
        }
    }
}
