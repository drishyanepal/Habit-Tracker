package com.application.habittracker.data.preferences

import android.content.Context

actual class AppPreferences actual constructor(private val context: Any?) {
    private val prefs = (context as Context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getTheme(): String = prefs.getString(KEY_THEME, "BLUE") ?: "BLUE"
    actual fun setTheme(value: String) { prefs.edit().putString(KEY_THEME, value).apply() }

    actual fun getNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    actual fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    }

    companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_THEME = "theme"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
