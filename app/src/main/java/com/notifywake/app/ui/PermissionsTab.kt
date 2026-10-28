package com.notifywake.app.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.notifywake.app.data.WakeSettings
import com.notifywake.app.util.StatusSnapshot
import com.notifywake.app.util.SystemStatus

data class PermissionCheck(
    val title: String,
    val grantedText: String,
    val missingText: String,
    val required: Boolean,
    val granted: Boolean,
    val actionLabel: String,
    val onAction: () -> Unit,
)

/** Everything the app needs, in display order. Shared by the Permissions tab, Home summary and nav badge. */
fun permissionChecks(
    ctx: Context,
    status: StatusSnapshot,
    settings: WakeSettings,
    requestNotifications: () -> Unit,
): List<PermissionCheck> = buildList {
    add(
        PermissionCheck(
            title = "Notification access",
            grantedText = "Granted. NotifyWake can see when notifications arrive.",
            missingText = "Lets NotifyWake notice new notifications. It never reads, stores or sends their content.",
            required = true,
            granted = status.notificationAccess,
            actionLabel = "Grant access",
            onAction = { SystemStatus.openNotificationAccess(ctx) },
        )
    )
    add(
        PermissionCheck(
            title = "Unrestricted battery",
            grantedText = "Android won't put NotifyWake to sleep.",
            missingText = "Without this, One UI may stop the app in the background.",
            required = true,
            granted = status.batteryUnrestricted,
            actionLabel = "Allow",
            onAction = { SystemStatus.requestUnrestrictedBattery(ctx) },
        )
    )
    if (status.turnScreenOnAllowed == false) {
        add(
            PermissionCheck(
                title = "Turn screen on",
                grantedText = "Allowed.",
                missingText = "Android has blocked this app from turning on the screen. In App info, allow " +
                    "\"Turn screen on\" (under Special access / Advanced).",
                required = true,
                granted = false,
                actionLabel = "Open App info",
                onAction = { SystemStatus.openAppDetails(ctx) },
            )
        )
    }
    add(
        PermissionCheck(
            title = "Show notifications",
            grantedText = "Allowed. Used for the test and the backup wake method.",
            missingText = "Needed for the test notification and the backup wake method.",
            required = false,
            granted = status.canPostNotifications,
            actionLabel = "Allow",
            onAction = requestNotifications,
        )
    )
    if (settings.backupWake && status.fullScreenIntentAllowed != null) {
        add(
            PermissionCheck(
                title = "Full-screen notifications",
                grantedText = "Allowed. The backup method can wake the screen.",
                missingText = "Lets the backup method wake the screen if the normal method is blocked.",
                required = false,
                granted = status.fullScreenIntentAllowed,
                actionLabel = "Allow",
                onAction = { SystemStatus.openFullScreenIntentSettings(ctx) },
            )
        )
    }
}

@Composable
fun PermissionsTab(status: StatusSnapshot, checks: List<PermissionCheck>) {
    val ctx = LocalContext.current
    val done = checks.count { it.granted }

    TabScaffold(title = "Permissions") {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (done == checks.size) "All set" else "$done of ${checks.size} ready",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                LinearProgressIndicator(
                    progress = { if (checks.isEmpty()) 1f else done.toFloat() / checks.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                Text(
                    "Grant these once. The app keeps working after you close it.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        listOf(true to "Required", false to "Recommended").forEach { (required, header) ->
            val group = checks.filter { it.required == required }
            if (group.isNotEmpty()) {
                SettingsGroup(header) {
                    group.forEach { c ->
                        PermissionListItem(
                            state = when {
                                c.granted -> CheckState.OK
                                c.required -> CheckState.MISSING_REQUIRED
                                else -> CheckState.MISSING_OPTIONAL
                            },
                            title = c.title,
                            description = if (c.granted) c.grantedText else c.missingText,
                            actionLabel = c.actionLabel,
                            onAction = c.onAction,
                        )
                    }
                }
            }
        }

        if (!status.notificationAccess && Build.VERSION.SDK_INT >= 33) {
            SettingsGroup("Can't turn on Notification access?") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Android blocks this for apps installed from an APK until you allow it:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.size(4.dp))
                }
                NumberedStep(1, "Open App info (button below).")
                NumberedStep(2, "Tap ⋮ in the top-right corner → Allow restricted settings.")
                NumberedStep(3, "Confirm with your PIN or fingerprint, then tap Grant access again.")
                TextButton(
                    onClick = { SystemStatus.openAppDetails(ctx) },
                    modifier = Modifier.padding(start = 8.dp),
                ) { Text("Open App info") }
            }
        }
    }
}
