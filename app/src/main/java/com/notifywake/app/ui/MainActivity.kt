package com.notifywake.app.ui

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings
import com.notifywake.app.service.WakeListenerService
import com.notifywake.app.util.SystemStatus

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = SettingsStore(this)

        // If access is granted but Samsung hasn't (re)bound the listener, nudge it.
        if (SystemStatus.hasNotificationAccess(this) && !WakeListenerService.connected.value) {
            WakeListenerService.requestRebindSafely(this)
        }

        setContent {
            NotifyWakeTheme {
                AppRoot(store)
            }
        }
    }
}

enum class Tab(val label: String, val selectedIcon: ImageVector, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    PERMISSIONS("Permissions", Icons.Filled.Lock, Icons.Outlined.Lock),
    TIPS("Tips", Icons.Filled.Info, Icons.Outlined.Info),
}

@Composable
private fun AppRoot(store: SettingsStore) {
    val ctx = LocalContext.current
    val settings by rememberWakeSettings(store)
    val connected by WakeListenerService.connected.collectAsStateWithLifecycle()

    var refreshTick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshTick++ }
    val status = remember(refreshTick) { SystemStatus.snapshot(ctx) }

    val notifPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshTick++ }
    val requestNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            SystemStatus.openAppNotificationSettings(ctx)
        }
    }
    val checks = permissionChecks(ctx, status, settings, requestNotifications)
    val pending = checks.count { !it.granted }

    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    if (showPicker) {
        BackHandler { showPicker = false }
        AppPickerScreen(store = store, onBack = { showPicker = false })
    } else {
        BackHandler(enabled = tab != Tab.HOME) { tab = Tab.HOME }
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            label = { Text(t.label) },
                            icon = {
                                BadgedBox(badge = {
                                    if (t == Tab.PERMISSIONS && pending > 0) Badge { Text("$pending") }
                                }) {
                                    Icon(if (tab == t) t.selectedIcon else t.icon, contentDescription = null)
                                }
                            },
                        )
                    }
                }
            },
        ) { outer ->
            Box(Modifier.padding(bottom = outer.calculateBottomPadding())) {
                when (tab) {
                    Tab.HOME -> HomeTab(
                        settings = settings,
                        status = status,
                        checks = checks,
                        connected = connected,
                        store = store,
                        onRequestNotifications = requestNotifications,
                        onNavigate = { tab = it },
                    )
                    Tab.SETTINGS -> SettingsTab(settings, store, onOpenPicker = { showPicker = true })
                    Tab.PERMISSIONS -> PermissionsTab(status, checks)
                    Tab.TIPS -> TipsTab(status)
                }
            }
        }
    }
}

/** Current settings, kept in sync with SharedPreferences (including writes from the service). */
@Composable
fun rememberWakeSettings(store: SettingsStore): State<WakeSettings> {
    val state = remember { mutableStateOf(store.load()) }
    DisposableEffect(store) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            state.value = store.load()
        }
        store.prefs.registerOnSharedPreferenceChangeListener(listener)
        state.value = store.load()
        onDispose { store.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return state
}
