package com.notifywake.app.util

import android.Manifest
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

data class StatusSnapshot(
    val notificationAccess: Boolean,
    val canPostNotifications: Boolean,
    val batteryUnrestricted: Boolean,
    /** null when the device doesn't have this permission (Android 13 and older). */
    val fullScreenIntentAllowed: Boolean?,
    /** null when unknown / not applicable. */
    val turnScreenOnAllowed: Boolean?,
)

object SystemStatus {

    fun snapshot(ctx: Context) = StatusSnapshot(
        notificationAccess = hasNotificationAccess(ctx),
        canPostNotifications = canPostNotifications(ctx),
        batteryUnrestricted = isIgnoringBatteryOptimizations(ctx),
        fullScreenIntentAllowed = if (Build.VERSION.SDK_INT >= 34) {
            ctx.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        } else null,
        turnScreenOnAllowed = turnScreenOnAllowed(ctx),
    )

    fun hasNotificationAccess(ctx: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)

    fun canPostNotifications(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= 33) {
            ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        }

    fun isIgnoringBatteryOptimizations(ctx: Context): Boolean =
        ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName)

    /** Android 14+ "Turn screen on" special access. Best effort: returns null if the op isn't known. */
    private fun turnScreenOnAllowed(ctx: Context): Boolean? {
        if (Build.VERSION.SDK_INT < 34) return null
        return try {
            val mode = ctx.getSystemService(AppOpsManager::class.java)
                .unsafeCheckOpNoThrow("android:turn_screen_on", Process.myUid(), ctx.packageName)
            mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_DEFAULT
        } catch (e: Exception) {
            null
        }
    }

    val isSamsung: Boolean get() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

    // ---- Settings shortcuts -------------------------------------------------------------------

    fun openNotificationAccess(ctx: Context) {
        val detail = if (Build.VERSION.SDK_INT >= 30) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                ComponentName(ctx, "com.notifywake.app.service.WakeListenerService").flattenToString()
            )
        } else null
        // Samsung handles the generic list most reliably, so prefer it.
        tryStart(ctx, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), detail)
    }

    fun requestUnrestrictedBattery(ctx: Context) {
        @Suppress("BatteryLife")
        val ask = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${ctx.packageName}")
        )
        tryStart(ctx, ask, appDetailsIntent(ctx))
    }

    fun openAppDetails(ctx: Context) = tryStart(ctx, appDetailsIntent(ctx))

    fun openAppNotificationSettings(ctx: Context) = tryStart(
        ctx,
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName),
        appDetailsIntent(ctx)
    )

    fun openFullScreenIntentSettings(ctx: Context) {
        val intent = if (Build.VERSION.SDK_INT >= 34) {
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:${ctx.packageName}")
            )
        } else null
        tryStart(ctx, intent, appDetailsIntent(ctx))
    }

    /** Samsung Device care → Battery, falling back to the generic battery optimization list. */
    fun openBatterySettings(ctx: Context) {
        val samsung = listOf(
            "com.samsung.android.sm.battery.ui.BatteryActivity",
            "com.samsung.android.sm.ui.battery.BatteryActivity",
        ).map {
            Intent().setComponent(ComponentName("com.samsung.android.lool", it))
        }
        tryStart(
            ctx,
            *samsung.toTypedArray(),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            appDetailsIntent(ctx)
        )
    }

    private fun appDetailsIntent(ctx: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))

    /** Starts the first intent that resolves and launches successfully. */
    private fun tryStart(ctx: Context, vararg intents: Intent?): Boolean {
        for (intent in intents) {
            if (intent == null) continue
            try {
                ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            } catch (_: Exception) {
                // try the next one
            }
        }
        return false
    }
}
