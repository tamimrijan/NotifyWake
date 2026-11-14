package com.notifywake.app.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

/** Everything the app needs, in display order. Shared by the Permissions tab, Home tile and nav badge. */
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

    TabScaffold(title = "Permissions", subtitle = "Grant these once") {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$done", style = MaterialTheme.typography.displayLarge)
                    Text(
                        " / ${checks.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (done == checks.size) "All set ✓" else "ready",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                LinearWavyProgressIndicator(
                    progress = { if (checks.isEmpty()) 1f else done.toFloat() / checks.size },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "The app keeps working after you close it or restart the phone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        listOf(true to "Required", false to "Recommended").forEach { (required, header) ->
            val group = checks.filter { it.required == required }
            if (group.isNotEmpty()) {
                SegmentedSection(header) {
                    group.forEachIndexed { i, c -> PermissionItem(i, group.size, c) }
                }
            }
        }

        if (!status.notificationAccess && Build.VERSION.SDK_INT >= 33) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(vertical = 18.dp)) {
                    Text(
                        "Can't turn on Notification access?",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Text(
                        "Android blocks this for apps installed from an APK until you allow it:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
                    )
                    NumberedStep(1, "Open App info (button below).")
                    NumberedStep(2, "Tap ⋮ in the top-right corner → Allow restricted settings.")
                    NumberedStep(3, "Confirm with your PIN or fingerprint, then tap Grant access again.")
                    Button(
                        onClick = { SystemStatus.openAppDetails(ctx) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.padding(start = 20.dp, top = 8.dp),
                    ) { Text("Open App info") }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(index: Int, count: Int, c: PermissionCheck) {
    val colors = MaterialTheme.colorScheme
    SegmentedListItem(
        onClick = { if (!c.granted) c.onAction() },
        shapes = ListItemDefaults.segmentedShapes(index, count),
        leadingContent = {
            when {
                c.granted -> ShapeIcon(
                    Icons.Filled.Check, MaterialShapes.Cookie9Sided,
                    container = StatusColors.ok, content = colors.surface,
                )
                c.required -> ShapeIcon(
                    Icons.Filled.Warning, MaterialShapes.SoftBurst,
                    container = colors.errorContainer, content = colors.onErrorContainer,
                )
                else -> ShapeIcon(
                    Icons.Filled.Info, MaterialShapes.Clover4Leaf,
                    container = colors.tertiaryContainer, content = colors.onTertiaryContainer,
                )
            }
        },
        supportingContent = {
            Column {
                Text(if (c.granted) c.grantedText else c.missingText)
                if (!c.granted) {
                    Spacer(Modifier.size(10.dp))
                    FilledTonalButton(onClick = c.onAction, shapes = ButtonDefaults.shapes()) {
                        Text(c.actionLabel)
                    }
                }
            }
        },
    ) { Text(c.title) }
}
