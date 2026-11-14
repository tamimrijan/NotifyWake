package com.notifywake.app.ui

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifywake.app.data.FilterMode
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings
import com.notifywake.app.service.TestNotifier
import com.notifywake.app.service.WakeListenerService
import com.notifywake.app.util.StatusSnapshot
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

    TabScaffold(title = "NotifyWake", subtitle = "Wake screen on notifications", snackbarHostState = snackbar) {
        HeroCard(settings, status, connected, onToggle = store::setEnabled)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionTile(checks, Modifier.weight(1f), onClick = { onNavigate(Tab.PERMISSIONS) })
            WakeCountTile(settings, Modifier.weight(1f))
        }

        TestCard(
            settings = settings,
            status = status,
            connected = connected,
            onMessage = { msg -> scope.launch { snackbar.showSnackbar(msg) } },
            onRequestNotifications = onRequestNotifications,
        )

        SegmentedSection("At a glance") {
            val chevron: @Composable () -> Unit = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            NavItem(
                index = 0, count = 2,
                title = "Apps that wake the screen",
                description = if (settings.filterMode == FilterMode.ALL) "All apps"
                else "${settings.selectedApps.size} selected app${if (settings.selectedApps.size == 1) "" else "s"}",
                icon = Icons.AutoMirrored.Outlined.List,
                trailing = chevron,
                onClick = { onNavigate(Tab.SETTINGS) },
            )
            NavItem(
                index = 1, count = 2,
                title = "Cooldown",
                description = cooldownLabel(settings.cooldownSeconds),
                icon = Icons.Outlined.DateRange,
                trailing = chevron,
                onClick = { onNavigate(Tab.SETTINGS) },
            )
        }

        CreditFooter()
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
        !status.notificationAccess -> "Setup" to "Grant Notification access in the Permissions tab."
        !settings.enabled -> "Paused" to "New notifications won't turn the screen on."
        !connected -> "Starting" to "Waiting for Android to connect the listener…"
        else -> "Active" to "Your screen lights up when a new notification arrives."
    }
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (active) colors.primary else colors.surfaceContainerHigh,
        MaterialTheme.motionScheme.defaultEffectsSpec(), label = "heroContainer",
    )
    val onContainer by animateColorAsState(
        if (active) colors.onPrimary else colors.onSurface,
        MaterialTheme.motionScheme.defaultEffectsSpec(), label = "heroContent",
    )

    // Slowly spinning cookie while active, calm circle while paused.
    val spin = rememberInfiniteTransition(label = "spin")
    val rotation by spin.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )

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
                AnimatedContent(targetState = active, label = "heroShape") { on ->
                    Box(
                        Modifier
                            .size(72.dp)
                            .graphicsLayer { rotationZ = if (on) rotation else 0f }
                            .clip((if (on) MaterialShapes.Cookie9Sided else MaterialShapes.Circle).toShape())
                            .background(if (on) colors.primaryContainer else colors.outlineVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = if (on) colors.onPrimaryContainer else colors.onSurfaceVariant,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer { rotationZ = if (on) -rotation else 0f },
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = onToggle,
                    thumbContent = {
                        Icon(
                            if (settings.enabled) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = colors.primaryContainer,
                        checkedThumbColor = colors.primary,
                        checkedIconColor = colors.primaryContainer,
                    ),
                )
            }
            Spacer(Modifier.size(28.dp))
            AnimatedContent(targetState = headline, label = "headline") { text ->
                Text(text, style = MaterialTheme.typography.displayLarge)
            }
            Spacer(Modifier.size(6.dp))
            Text(detail, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PermissionTile(checks: List<PermissionCheck>, modifier: Modifier, onClick: () -> Unit) {
    val done = checks.count { it.granted }
    val total = checks.size.coerceAtLeast(1)
    val allSet = done == checks.size
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (allSet) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
        ),
        modifier = modifier.aspectRatio(1f),
    ) {
        Column(Modifier.padding(18.dp)) {
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { done.toFloat() / total },
                    modifier = Modifier.size(64.dp),
                )
                Text("$done/${checks.size}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.weight(1f))
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            Text(
                if (allSet) "All set" else "${checks.size - done} to fix",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun WakeCountTile(settings: WakeSettings, modifier: Modifier) {
    val ctx = LocalContext.current
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.aspectRatio(1f),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "${settings.wakeCount}",
                style = MaterialTheme.typography.displayMedium,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            Text("Screen wakes", style = MaterialTheme.typography.titleMedium)
            val last = settings.lastWakeApp
            Text(
                if (last == null) "None yet"
                else "${appLabel(ctx, last)}, " + DateUtils.getRelativeTimeSpanString(
                    settings.lastWakeAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                ),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun TestCard(
    settings: WakeSettings,
    status: StatusSnapshot,
    connected: Boolean,
    onMessage: (String) -> Unit,
    onRequestNotifications: () -> Unit,
) {
    val ctx = LocalContext.current
    var runId by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val running = progress.value > 0f

    LaunchedEffect(runId) {
        if (runId == 0) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        progress.snapTo(1f)
        progress.animateTo(0f, tween(TEST_DELAY_SECONDS * 1000, easing = LinearEasing))
    }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Test it", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.size(6.dp))
            Text(
                if (running) "Turn the screen off now"
                else "Tap Start, then press the side button. In $TEST_DELAY_SECONDS seconds your screen " +
                    "should light up on the lock screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(20.dp))

            AnimatedContent(targetState = running, label = "test") { isRunning ->
                if (isRunning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator(
                                progress = { progress.value },
                                modifier = Modifier.size(150.dp),
                            )
                            Text(
                                "${kotlin.math.ceil(progress.value * TEST_DELAY_SECONDS).toInt()}",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.size(16.dp))
                        OutlinedButton(
                            onClick = {
                                TestNotifier.cancel()
                                runId = 0
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) { Text("Cancel") }
                    }
                } else {
                    val height = ButtonDefaults.MediumContainerHeight
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
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.contentPaddingFor(height),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = height),
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)),
                        )
                        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
                        Text("Start test", style = ButtonDefaults.textStyleFor(height))
                    }
                }
            }
        }
    }
}

@Composable
fun CreditFooter() {
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
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Built by $DEVELOPER_NAME",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun appLabel(ctx: Context, pkg: String): String = try {
    if (pkg == ctx.packageName) "Test"
    else ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(pkg, 0)).toString()
} catch (_: Exception) {
    pkg.substringAfterLast('.')
}
