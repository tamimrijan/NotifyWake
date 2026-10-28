package com.notifywake.app.data

import android.content.Context
import android.content.SharedPreferences

enum class FilterMode { ALL, SELECTED }

data class WakeSettings(
    val enabled: Boolean,
    val filterMode: FilterMode,
    val selectedApps: Set<String>,
    /** Minimum gap between two screen wakes. 0 = no cooldown. */
    val cooldownSeconds: Int,
    /** How long the screen is held on (at minimum) after a wake. */
    val screenOnSeconds: Int,
    val ignoreSilent: Boolean,
    val ignoreOngoing: Boolean,
    val respectDnd: Boolean,
    val backupWake: Boolean,
    val wakeCount: Int,
    val lastWakeAt: Long,
    val lastWakeApp: String?,
)

/**
 * SharedPreferences-backed settings. The listener service reads them on every notification
 * (SharedPreferences keeps them in memory, so this is cheap) and the UI observes changes.
 */
class SettingsStore(context: Context) {

    val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(): WakeSettings = WakeSettings(
        enabled = prefs.getBoolean(K_ENABLED, true),
        filterMode = if (prefs.getString(K_FILTER, null) == FilterMode.SELECTED.name) {
            FilterMode.SELECTED
        } else {
            FilterMode.ALL
        },
        selectedApps = prefs.getStringSet(K_SELECTED, null)?.toSet() ?: emptySet(),
        cooldownSeconds = prefs.getInt(K_COOLDOWN, 0),
        screenOnSeconds = prefs.getInt(K_SCREEN_ON, 5),
        ignoreSilent = prefs.getBoolean(K_IGNORE_SILENT, true),
        ignoreOngoing = prefs.getBoolean(K_IGNORE_ONGOING, true),
        respectDnd = prefs.getBoolean(K_RESPECT_DND, true),
        backupWake = prefs.getBoolean(K_BACKUP, true),
        wakeCount = prefs.getInt(K_COUNT, 0),
        lastWakeAt = prefs.getLong(K_LAST_AT, 0L),
        lastWakeApp = prefs.getString(K_LAST_APP, null),
    )

    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) =
        prefs.edit().apply(block).apply()

    fun setEnabled(v: Boolean) = edit { putBoolean(K_ENABLED, v) }
    fun setFilterMode(v: FilterMode) = edit { putString(K_FILTER, v.name) }
    fun setSelectedApps(v: Set<String>) = edit { putStringSet(K_SELECTED, HashSet(v)) }
    fun setCooldownSeconds(v: Int) = edit { putInt(K_COOLDOWN, v) }
    fun setScreenOnSeconds(v: Int) = edit { putInt(K_SCREEN_ON, v) }
    fun setIgnoreSilent(v: Boolean) = edit { putBoolean(K_IGNORE_SILENT, v) }
    fun setIgnoreOngoing(v: Boolean) = edit { putBoolean(K_IGNORE_ONGOING, v) }
    fun setRespectDnd(v: Boolean) = edit { putBoolean(K_RESPECT_DND, v) }
    fun setBackupWake(v: Boolean) = edit { putBoolean(K_BACKUP, v) }

    fun recordWake(packageName: String) = edit {
        putInt(K_COUNT, prefs.getInt(K_COUNT, 0) + 1)
        putLong(K_LAST_AT, System.currentTimeMillis())
        putString(K_LAST_APP, packageName)
    }

    private companion object {
        const val FILE = "wake_settings"
        const val K_ENABLED = "enabled"
        const val K_FILTER = "filter_mode"
        const val K_SELECTED = "selected_apps"
        const val K_COOLDOWN = "cooldown_seconds"
        const val K_SCREEN_ON = "screen_on_seconds"
        const val K_IGNORE_SILENT = "ignore_silent"
        const val K_IGNORE_ONGOING = "ignore_ongoing"
        const val K_RESPECT_DND = "respect_dnd"
        const val K_BACKUP = "backup_wake"
        const val K_COUNT = "wake_count"
        const val K_LAST_AT = "last_wake_at"
        const val K_LAST_APP = "last_wake_app"
    }
}
