package com.notifywake.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.notifywake.app.BuildConfig
import com.notifywake.app.util.StatusSnapshot
import com.notifywake.app.util.SystemStatus

private val troubleshooting = listOf(
    "Status says \"Starting\" for a long time" to
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

    TabScaffold(title = "Tips", subtitle = "Keep it running smoothly") {
        Column {
            SectionHeader("Samsung: keep it running")
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(vertical = 14.dp)) {
                    Text(
                        if (SystemStatus.isSamsung) "One UI freezes background apps aggressively. Do these once."
                        else "Recommended on Samsung and other phones with aggressive battery savers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                    NumberedStep(1, "Settings → Apps → NotifyWake → Battery → Unrestricted.")
                    NumberedStep(
                        2, "Settings → Battery → Background usage limits: remove NotifyWake from " +
                            "Sleeping / Deep sleeping apps, add it to Never sleeping apps."
                    )
                    NumberedStep(3, "Optionally turn off \"Put unused apps to sleep\".")
                    NumberedStep(4, "Recents: tap the NotifyWake icon above its card → Keep open.")
                    NumberedStep(5, "Settings → Lock screen → Notifications: show them on the lock screen.")
                    NumberedStep(6, "App info: turn off \"Pause app activity if unused\".")
                    Row(
                        Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { SystemStatus.openAppDetails(ctx) },
                            shapes = ButtonDefaults.shapes(),
                        ) { Text("App info") }
                        OutlinedButton(
                            onClick = { SystemStatus.openBatterySettings(ctx) },
                            shapes = ButtonDefaults.shapes(),
                        ) { Text("Battery settings") }
                    }
                    if (!status.batteryUnrestricted) {
                        Text(
                            "Battery is still restricted. See the Permissions tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }

        SegmentedSection(header = "Troubleshooting") {
            troubleshooting.forEachIndexed { i, (q, a) ->
                ExpandableTip(i, troubleshooting.size, q, a)
            }
        }

        SegmentedSection(header = "Privacy & security") {
            InfoItem(0, 3, Icons.Outlined.Lock, "Your phone stays locked",
                "Only the display turns on. PIN, password and fingerprint are still required.")
            InfoItem(1, 3, Icons.Outlined.Info, "Nothing leaves your phone",
                "Notification content is never read or stored, and the app has no internet permission.")
            InfoItem(2, 3, Icons.Outlined.Phone, "Quiet during calls",
                "The screen never wakes while you're on a call.")
        }

        AboutCard()
    }
}

@Composable
private fun InfoItem(index: Int, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String) {
    SegmentedListItem(
        onClick = {},
        enabled = true,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        leadingContent = {
            ShapeIcon(
                icon, MaterialShapes.Cookie9Sided,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        supportingContent = { Text(text) },
    ) { Text(title) }
}

@Composable
private fun ExpandableTip(index: Int, count: Int, question: String, answer: String) {
    var open by rememberSaveable(question) { mutableStateOf(false) }
    val arrow by animateFloatAsState(
        if (open) 180f else 0f, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "arrow",
    )
    SegmentedListItem(
        onClick = { open = !open },
        shapes = ListItemDefaults.segmentedShapes(index, count),
        supportingContent = {
            AnimatedVisibility(open) {
                Text(answer, modifier = Modifier.padding(top = 4.dp))
            }
        },
        trailingContent = {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = if (open) "Collapse" else "Expand",
                modifier = Modifier.rotate(arrow),
            )
        },
    ) { Text(question) }
}

@Composable
private fun AboutCard() {
    val ctx = LocalContext.current
    Column {
        SectionHeader("About")
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val icon = remember {
                    ctx.packageManager.getApplicationIcon(ctx.packageName).toBitmap(192, 192).asImageBitmap()
                }
                Image(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape()),
                )
                Spacer(Modifier.size(14.dp))
                Text("NotifyWake", style = MaterialTheme.typography.displaySmall)
                Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                Spacer(Modifier.size(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShapeIcon(
                        Icons.Outlined.Person, MaterialShapes.Sunny,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        size = 48.dp, iconSize = 24.dp,
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Designed & built by", style = MaterialTheme.typography.labelLarge)
                        Text(DEVELOPER_NAME, style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Spacer(Modifier.size(16.dp))
                Text(
                    "Lights up your screen for new notifications, like an iPhone.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
