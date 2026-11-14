package com.notifywake.app.ui

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.notifywake.app.data.FilterMode
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings

@Composable
fun SettingsTab(settings: WakeSettings, store: SettingsStore, onOpenPicker: () -> Unit) {
    TabScaffold(title = "Settings", subtitle = "Make it work your way") {
        val selecting = settings.filterMode == FilterMode.SELECTED
        SegmentedSection(
            header = "Apps",
            footer = if (!selecting) "Filtering is off: every app's notifications wake the screen." else null,
        ) {
            ChoiceBlock(
                title = "Which apps can wake the screen",
                description = null,
                shapeIndex = 0,
                shapeCount = if (selecting) 2 else 1,
            ) {
                ConnectedChoice(
                    options = listOf(FilterMode.ALL to "All apps", FilterMode.SELECTED to "Selected"),
                    selected = settings.filterMode,
                    onSelect = store::setFilterMode,
                )
            }
            if (selecting) {
                val count = settings.selectedApps.size
                NavItem(
                    index = 1, count = 2,
                    title = "Choose apps",
                    description = if (count == 0) "None selected, so nothing will wake the screen"
                    else "$count app${if (count == 1) "" else "s"} selected",
                    icon = Icons.Outlined.Edit,
                    trailing = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    onClick = onOpenPicker,
                )
                if (count == 0) {
                    Text(
                        "Pick at least one app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        SegmentedSection(header = "Timing") {
            ChoiceBlock(
                title = "Cooldown between wakes",
                description = "Stops a burst of messages from waking the screen again and again.",
                shapeIndex = 0, shapeCount = 2,
            ) {
                ConnectedChoice(
                    options = listOf(0 to "Off", 5 to "5s", 10 to "10s", 30 to "30s", 60 to "1m", 300 to "5m"),
                    selected = settings.cooldownSeconds,
                    onSelect = store::setCooldownSeconds,
                )
            }
            ChoiceBlock(
                title = "Keep screen on for at least",
                description = "After this, your normal lock-screen timeout applies.",
                shapeIndex = 1, shapeCount = 2,
            ) {
                ConnectedChoice(
                    options = listOf(3 to "3s", 5 to "5s", 8 to "8s", 15 to "15s"),
                    selected = settings.screenOnSeconds,
                    onSelect = store::setScreenOnSeconds,
                )
            }
        }

        SegmentedSection(header = "Filters") {
            SwitchItem(
                0, 3, "Ignore silent notifications",
                "Skip low-priority notifications that don't sound or pop up",
                Icons.Outlined.Notifications, settings.ignoreSilent, store::setIgnoreSilent,
            )
            SwitchItem(
                1, 3, "Ignore ongoing notifications",
                "Music, downloads, navigation, \"app is running\"",
                Icons.Outlined.PlayArrow, settings.ignoreOngoing, store::setIgnoreOngoing,
            )
            SwitchItem(
                2, 3, "Respect Do Not Disturb",
                "Stay dark for notifications DND or Sleep mode silences",
                Icons.Outlined.Face, settings.respectDnd, store::setRespectDnd,
            )
        }

        SegmentedSection(
            header = "Wake method",
            footer = "The screen never wakes during a phone call. The phone always stays locked: " +
                "your PIN, password and fingerprint are still required.",
        ) {
            SwitchItem(
                0, 1, "Backup wake method",
                "If the screen is still off after a moment, wake it with a silent full-screen alert",
                Icons.Outlined.Refresh, settings.backupWake, store::setBackupWake,
            )
        }

        if (Build.VERSION.SDK_INT >= 31) {
            SegmentedSection(header = "Appearance") {
                SwitchItem(
                    0, 1, "Match wallpaper colors",
                    "Use your phone's Material You colors instead of NotifyWake purple",
                    Icons.Outlined.Star, settings.wallpaperColors, store::setWallpaperColors,
                )
            }
        }
    }
}
