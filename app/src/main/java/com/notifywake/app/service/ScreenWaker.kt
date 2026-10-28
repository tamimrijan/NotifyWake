package com.notifywake.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.notifywake.app.R

/**
 * Turns the display on without touching the keyguard.
 *
 * Primary path: a screen wake lock with ACQUIRE_CAUSES_WAKEUP. This is what lights the screen on
 * almost every phone, Samsung included, and shows the normal lock screen with the new notification.
 *
 * Backup path: if the screen is still off shortly afterwards (some firmware ignores the flag),
 * post a silent full-screen-intent notification that launches [WakeActivity]. That activity is
 * transparent, asks the system to turn the screen on, and finishes immediately — the lock screen
 * remains locked and visible behind it.
 */
object ScreenWaker {

    private const val TAG = "NotifyWake"
    private const val BACKUP_CHECK_DELAY_MS = 700L
    const val BACKUP_CHANNEL_ID = "wake_backup"
    const val BACKUP_NOTIFICATION_ID = 4242

    private val handler = Handler(Looper.getMainLooper())

    fun isScreenOn(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java).isInteractive

    @Suppress("DEPRECATION") // SCREEN_BRIGHT_WAKE_LOCK / ACQUIRE_CAUSES_WAKEUP: still the only way to wake from a service
    fun wake(context: Context, holdSeconds: Int, useBackup: Boolean) {
        val app = context.applicationContext
        val pm = app.getSystemService(PowerManager::class.java)

        try {
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                "NotifyWake:screen"
            )
            wakeLock.setReferenceCounted(false)
            // Timed acquire: Android releases it for us, so the screen can never be stuck on.
            wakeLock.acquire(holdSeconds.coerceIn(1, 30) * 1000L)
        } catch (e: Exception) {
            Log.w(TAG, "Wake lock failed", e)
        }

        if (useBackup) {
            handler.postDelayed({
                if (!pm.isInteractive) {
                    Log.i(TAG, "Screen still off after wake lock; using full-screen-intent backup")
                    fireBackup(app)
                }
            }, BACKUP_CHECK_DELAY_MS)
        }
    }

    private fun fireBackup(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        if (Build.VERSION.SDK_INT >= 34 && !nm.canUseFullScreenIntent()) return

        ensureBackupChannel(context)
        val intent = Intent(context, WakeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = Notification.Builder(context, BACKUP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_wake)
            .setContentTitle("Waking screen")
            .setCategory(Notification.CATEGORY_REMINDER)
            .setFullScreenIntent(pending, true)
            .setTimeoutAfter(4000)
            .setOnlyAlertOnce(true)
            .build()
        nm.notify(BACKUP_NOTIFICATION_ID, notification)
    }

    private fun ensureBackupChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(BACKUP_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            BACKUP_CHANNEL_ID,
            context.getString(R.string.channel_backup),
            NotificationManager.IMPORTANCE_HIGH // full-screen intents require high importance
        ).apply {
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }
}
