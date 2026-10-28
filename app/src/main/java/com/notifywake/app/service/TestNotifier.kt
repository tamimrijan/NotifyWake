package com.notifywake.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.notifywake.app.R
import com.notifywake.app.ui.MainActivity

/**
 * Posts a real notification after a delay so the whole pipeline is exercised:
 * notification posted → listener receives it → screen wakes.
 *
 * The process stays alive while the screen is off because the system holds the listener binding.
 */
object TestNotifier {

    const val CHANNEL_ID = "wake_test"
    private const val NOTIFICATION_ID = 1001

    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null

    fun schedule(context: Context, delayMs: Long) {
        val app = context.applicationContext
        cancel()
        val r = Runnable { post(app) }
        pending = r
        handler.postDelayed(r, delayMs)
    }

    fun cancel() {
        pending?.let { handler.removeCallbacks(it) }
        pending = null
    }

    private fun post(context: Context) {
        pending = null
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_test),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_wake)
            .setContentTitle("NotifyWake test")
            .setContentText("If your screen lit up by itself, it works!")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }
}
