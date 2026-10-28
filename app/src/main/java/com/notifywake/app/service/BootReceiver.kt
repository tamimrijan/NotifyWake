package com.notifywake.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notifywake.app.util.SystemStatus

/**
 * Android normally re-binds an enabled notification listener by itself after boot. Samsung's
 * battery manager occasionally delays that, so nudge it explicitly after boot and after updates.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (SystemStatus.hasNotificationAccess(context)) {
                    WakeListenerService.requestRebindSafely(context)
                }
            }
        }
    }
}
