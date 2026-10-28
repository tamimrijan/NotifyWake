package com.notifywake.app.ui

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifywake.app.data.FilterMode
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings
import com.notifywake.app.service.TestNotifier
import com.notifywake.app.service.WakeListenerService
import com.notifywake.app.util.StatusSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TEST_DELAY_SECONDS = 8
const val DEVELOPER_NAME = "Tamim Ahmed Rijan"

@Composable
fun HomeTab(
    settings: WakeSettings,
    status: StatusSnapshot,
    checks: List<PermissionCheck>,
    connected: Boolean,
    store: SettingsStore,
    onRequestNotifications: () -> Unit,
    onNavigate: (Tab) -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    TabScaffold(title = "NotifyWake", snackbarHostState = snackbar) {
        HeroCard(settings, status, connected, onToggle = store::setEnabled)

        SetupSummary(checks, onOpen = { onNavigate(Tab.PERMISSIONS) })

        SettingsGroup("At a glance") {
            ListItem(
                headlineContent = { Text("Apps that wake the screen") },
                supportingContent = {
                    Text(
                        if (settings.filterMode == FilterMode.ALL) "All apps"
                        else "${settings.selectedApps.size} selected app${if (settings.selectedApps.size == 1) "" else "s"}"
                    )
                },
                leadingContent = { Icon(Icons.AutoMirrored.Outlined.List,contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                colors = transparentListItemColors(),
                modifier = Modifier.clickable { onNavigate(Tab.SETTINGS) },
            )
            ListItem(
                headlineContent = { Text("Cooldown") },
                supportingContent = { Text(cooldownLabel(settings.cooldownSeconds)) },
                leadingContent = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                colors = transparentListItemColors(),
                modifier = Modifier.clickable { onNavigate(Tab.SETTINGS) },
            )
        }

        TestGroup(
            settings = settings,
            status = status,
            connected = connected,
            onMessage = { msg -> scope.launch { snackbar.showSnackbar(msg) } },
            onRequestNotifications = onRequestNotifications,
        )

        ActivityGroup(settings)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Built by $DEVELOPER_NAME",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

fun cooldownLabel(seconds: Int): String = when {
    seconds <= 0 -> "Off"
    seconds < 60 -> "$seconds seconds"
    else -> "${seconds / 60} min"
}

@Composable
private fun HeroCard(
    settings: WakeSettings,
    status: StatusSnapshot,
    connected: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val active = settings.enabled && status.notificationAccess
    val (headline, detail) = when {
        !status.notificationAccess -> "Setup needed" to "Grant Notification access in the Permissions tab."
        !settings.enabled -> "Paused" to "New notifications won't turn the screen on."
        !connected -> "Starting…" to "Waiting for Android to connect the notification listener."
        else -> "Active" to "Your screen lights up when a new notification arrives."
    }
    val container = if (active) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val onContainer = if (active) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .clickable { onToggle(!settings.enabled) }
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface,
                    )
                }
                Spacer(Modifier.weight(1f))
                Switch(checked = settings.enabled, onCheckedChange = onToggle)
            }
            Spacer(Modifier.size(20.dp))
            Text("Wake screen on notifications", style = MaterialTheme.typography.titleMedium)
            Text(headline, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(4.dp))
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SetupSummary(checks: List<PermissionCheck>, onOpen: () -> Unit) {
    val missingRequired = checks.count { it.required && !it.granted }
    val missingAny = checks.count { !it.granted }
    val (icon, tint, title, text) = when {
        missingRequired > 0 -> Quad(
            Icons.Filled.Warning, MaterialTheme.colorScheme.error,
            "$missingRequired required permission${if (missingRequired == 1) "" else "s"} missing",
            "Tap to fix in the Permissions tab.",
        )
        missingAny > 0 -> Quad(
            Icons.Filled.Warning, StatusColors.warn,
            "Almost ready",
            "$missingAny recommended permission${if (missingAny == 1) "" else "s"} not granted yet.",
        )
        else -> Quad(
            Icons.Filled.CheckCircle, StatusColors.ok,
            "All permissions granted",
            "Everything NotifyWake needs is in place.",
        )
    }
    SettingsGroup(header = null) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(text) },
            leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            colors = transparentListItemColors(),
            modifier = Modifier.clickable(onClick = onOpen),
        )
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
private fun TestGroup(
    settings: WakeSettings,
    status: StatusSnapshot,
    connected: Boolean,
    onMessage: (String) -> Unit,
    onRequestNotifications: () -> Unit,
) {
    val ctx = LocalContext.current
    var remaining by rememberSaveable { mutableIntStateOf(0) }
    var runId by remember { mutableIntStateOf(0) }

    LaunchedEffect(runId) {
        if (runId == 0) return@LaunchedEffect
        remaining = TEST_DELAY_SECONDS
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
    }

    SettingsGroup(
        header = "Test it",
        footer = "Screen stayed dark? Check the Permissions tab, then the Samsung steps in Tips.",
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                "Tap Start, then press the side button to turn the screen off. After " +
                    "$TEST_DELAY_SECONDS seconds a test notification arrives and your screen should " +
                    "light up on the lock screen.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.size(14.dp))
            if (remaining > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {
                        TestNotifier.cancel()
                        remaining = 0
                        runId = 0
                    }) { Text("Cancel") }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Screen off now… $remaining",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Button(
                    onClick = {
                        when {
                            !settings.enabled -> onMessage("Turn on the main switch first.")
                            !status.notificationAccess -> onMessage("Grant Notification access first.")
                            !status.canPostNotifications -> {
                                onMessage("Allow notifications so the test can be shown.")
                                onRequestNotifications()
                            }
                            else -> {
                                if (!connected) WakeListenerService.requestRebindSafely(ctx)
                                TestNotifier.schedule(ctx, TEST_DELAY_SECONDS * 1000L)
                                runId++
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start test")
                }
            }
        }
    }
}

@Composable
private fun ActivityGroup(settings: WakeSettings) {
    val ctx = LocalContext.current
    SettingsGroup("Activity") {
        if (settings.wakeCount == 0 || settings.lastWakeApp == null) {
            ListItem(
                headlineContent = { Text("No wakes yet") },
                supportingContent = { Text("Run the test above to try it.") },
                colors = transparentListItemColors(),
            )
        } else {
            val app = remember(settings.lastWakeApp) { appLabel(ctx, settings.lastWakeApp) }
            val whenText = DateUtils.getRelativeTimeSpanString(
                settings.lastWakeAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            )
            ListItem(
                overlineContent = { Text("Screen woken") },
                headlineContent = {
                    Text(
                        "${settings.wakeCount} time${if (settings.wakeCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                supportingContent = { Text("Last: $app, $whenText") },
                colors = transparentListItemColors(),
            )
        }
    }
}

private fun appLabel(ctx: Context, pkg: String): String = try {
    if (pkg == ctx.packageName) "Test notification"
    else ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(pkg, 0)).toString()
} catch (_: Exception) {
    pkg
}
