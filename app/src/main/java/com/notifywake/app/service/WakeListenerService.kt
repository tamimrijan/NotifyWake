package com.notifywake.app.service

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.notifywake.app.data.FilterMode
import com.notifywake.app.data.SettingsStore
import com.notifywake.app.data.WakeSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bound by Android itself once the user grants Notification access. Because the system holds the
 * binding, it keeps running after the app is closed or swiped from Recents, and Android re-binds it
 * after a reboot.
 */
class WakeListenerService : NotificationListenerService() {

    private lateinit var store: SettingsStore
    private val ranking = Ranking()

    /** Keys currently posted, so updates to an existing notification can be told apart from new ones. */
    private val activeKeys = HashSet<String>()
    private var lastWakeElapsed = 0L

    override fun onCreate() {
        super.onCreate()
        store = SettingsStore(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _connected.value = true
        activeKeys.clear()
        try {
            activeNotifications?.forEach { activeKeys.add(it.key) }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read active notifications", e)
        }
        Log.i(TAG, "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _connected.value = false
        Log.i(TAG, "Listener disconnected, asking Android to rebind")
        requestRebindSafely(this)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        activeKeys.remove(sbn.key)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap?) {
        val isUpdate = !activeKeys.add(sbn.key)
        val settings = store.load()

        val skipReason = skipReason(sbn, rankingMap, settings, isUpdate)
        if (skipReason != null) {
            Log.d(TAG, "Not waking for ${sbn.packageName}: $skipReason")
            return
        }

        lastWakeElapsed = SystemClock.elapsedRealtime()
        Log.i(TAG, "Waking screen for ${sbn.packageName}")
        ScreenWaker.wake(this, settings.screenOnSeconds, settings.backupWake)
        store.recordWake(sbn.packageName)
    }

    /** Returns why this notification should NOT wake the screen, or null to wake. */
    private fun skipReason(
        sbn: StatusBarNotification,
        rankingMap: RankingMap?,
        s: WakeSettings,
        isUpdate: Boolean,
    ): String? {
        if (!s.enabled) return "disabled"

        val isTest = sbn.packageName == packageName &&
            sbn.notification.channelId == TestNotifier.CHANNEL_ID
        if (sbn.packageName == packageName && !isTest) return "own notification"

        if (ScreenWaker.isScreenOn(this)) return "screen already on"
        if (isInCall()) return "phone call in progress"

        // The test notification checks the wake path itself, so it skips the user's filters.
        if (isTest) return null

        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return "group summary"
        if (s.ignoreOngoing &&
            (sbn.isOngoing || n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0)
        ) return "ongoing"
        if (isUpdate && n.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0) return "silent update"

        if (s.filterMode == FilterMode.SELECTED && sbn.packageName !in s.selectedApps) {
            return "app not selected"
        }

        if (rankingMap != null && rankingMap.getRanking(sbn.key, ranking)) {
            if (s.respectDnd && !ranking.matchesInterruptionFilter()) return "blocked by Do Not Disturb"
            if (s.ignoreSilent && ranking.importance < NotificationManager.IMPORTANCE_DEFAULT) {
                return "silent / low-priority"
            }
            if (Build.VERSION.SDK_INT >= 28 && ranking.isSuspended) return "app suspended"
        }

        if (s.cooldownSeconds > 0 &&
            SystemClock.elapsedRealtime() - lastWakeElapsed < s.cooldownSeconds * 1000L
        ) return "cooldown"

        return null
    }

    /** Never light the screen while the phone is against someone's ear. */
    private fun isInCall(): Boolean {
        val mode = getSystemService(AudioManager::class.java).mode
        return mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION
    }

    companion object {
        private const val TAG = "NotifyWake"

        private val _connected = MutableStateFlow(false)
        /** Whether Android currently has the listener bound (only meaningful in this process). */
        val connected: StateFlow<Boolean> = _connected

        fun requestRebindSafely(context: Context) {
            try {
                requestRebind(ComponentName(context, WakeListenerService::class.java))
            } catch (e: Exception) {
                Log.w(TAG, "requestRebind failed", e)
            }
        }
    }
}
