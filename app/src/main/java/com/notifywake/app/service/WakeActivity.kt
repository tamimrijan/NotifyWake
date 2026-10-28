package com.notifywake.app.service

import android.app.Activity
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager

/**
 * Transparent, short-lived activity used only by the backup wake path.
 *
 * It turns the screen on and shows *over* the lock screen without dismissing it
 * (no requestDismissKeyguard, no FLAG_DISMISS_KEYGUARD), then finishes. The phone stays locked,
 * and PIN / pattern / fingerprint are still required to get in.
 */
class WakeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        getSystemService(NotificationManager::class.java).cancel(ScreenWaker.BACKUP_NOTIFICATION_ID)
        window.decorView.postDelayed({ finish() }, 500)
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}
