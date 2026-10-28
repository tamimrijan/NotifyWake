package com.notifywake.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.notifywake.app.BuildConfig
import com.notifywake.app.util.StatusSnapshot
import com.notifywake.app.util.SystemStatus

private val troubleshooting = listOf(
    "Status says \"Starting…\" for a long time" to
        "Turn Notification access off and on again in the Permissions tab, or restart the phone.",
    "Test notification appears but the screen stays dark" to
        "Allow Full-screen notifications and keep Backup wake method on. Also check for a " +
        "\"Turn screen on\" item in Permissions.",
    "Works for a while, then stops" to
        "Samsung put the app to sleep. Redo the Samsung steps above: Unrestricted battery, " +
        "Never sleeping apps, and Keep open in Recents.",
    "WhatsApp or other messages arrive late" to
        "That app is being put to sleep, not NotifyWake. Add it to Never sleeping apps too.",
    "Screen wakes too often" to
        "Set a cooldown, switch to Selected apps, and keep Ignore silent notifications on.",
    "Screen wakes at night" to
        "Use Samsung's Sleep mode or Do Not Disturb, with Respect Do Not Disturb on.",
    "\"Unsafe app blocked\" by Play Protect" to
        "This appears because the APK is installed outside the Play Store. Tap More details → " +
        "Install anyway. It only appears when you install or update.",
)

@Composable
fun TipsTab(status: StatusSnapshot) {
    val ctx = LocalContext.current

    TabScaffold(title = "Tips") {
        SettingsGroup(
            header = "Samsung: keep it running",
            footer = if (SystemStatus.isSamsung) {
                "One UI freezes background apps aggressively. Do these once."
            } else {
                "Recommended on Samsung and other phones with aggressive battery savers."
            },
        ) {
            NumberedStep(1, "Settings → Apps → NotifyWake → Battery → Unrestricted.")
            NumberedStep(
                2, "Settings → Battery → Background usage limits: remove NotifyWake from Sleeping / " +
                    "Deep sleeping apps and add it to Never sleeping apps."
            )
            NumberedStep(3, "Optionally turn off \"Put unused apps to sleep\" on the same screen.")
            NumberedStep(4, "Recents: tap the NotifyWake icon above its card → Keep open.")
            NumberedStep(
                5, "Settings → Lock screen → Notifications: show notifications on the lock screen, " +
                    "so there's something to see when it lights up."
            )
            NumberedStep(6, "App info: turn off \"Pause app activity if unused\" if you see it.")
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { SystemStatus.openAppDetails(ctx) }) { Text("App info") }
                OutlinedButton(onClick = { SystemStatus.openBatterySettings(ctx) }) { Text("Battery settings") }
            }
            if (!status.batteryUnrestricted) {
                Text(
                    "Battery is still restricted. See the Permissions tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
        }

        SettingsGroup(header = "Troubleshooting") {
            troubleshooting.forEachIndexed { i, (q, a) ->
                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ExpandableTip(q, a)
            }
        }

        SettingsGroup(header = "Privacy & security") {
            ListItem(
                headlineContent = { Text("Your phone stays locked") },
                supportingContent = { Text("Only the display turns on. PIN, password and fingerprint are still required.") },
                leadingContent = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                colors = transparentListItemColors(),
            )
            ListItem(
                headlineContent = { Text("Nothing leaves your phone") },
                supportingContent = {
                    Text("Notification content is never read or stored, and the app has no internet permission.")
                },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                colors = transparentListItemColors(),
            )
            ListItem(
                headlineContent = { Text("Quiet during calls") },
                supportingContent = { Text("The screen never wakes while you're on a call.") },
                leadingContent = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                colors = transparentListItemColors(),
            )
        }

        AboutCard()
    }
}

@Composable
private fun ExpandableTip(question: String, answer: String) {
    var open by rememberSaveable(question) { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { open = !open }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(question, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(
                if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (open) "Collapse" else "Expand",
            )
        }
        AnimatedVisibility(open) {
            Text(
                answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, end = 24.dp),
            )
        }
    }
}

@Composable
private fun AboutCard() {
    val ctx = LocalContext.current
    Column {
        Text(
            "About",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val icon = remember {
                    ctx.packageManager.getApplicationIcon(ctx.packageName).toBitmap(144, 144).asImageBitmap()
                }
                Image(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.size(12.dp))
                Text("NotifyWake", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(16.dp))
                HorizontalDivider()
                Spacer(Modifier.size(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text("Designed & built by", style = MaterialTheme.typography.labelMedium)
                        Text(
                            DEVELOPER_NAME,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    "Lights up your screen for new notifications, like an iPhone.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
