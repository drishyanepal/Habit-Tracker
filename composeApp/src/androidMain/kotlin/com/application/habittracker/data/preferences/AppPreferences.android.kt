package com.application.habittracker.data.preferences

import android.content.Context

actual class AppPreferences actual constructor(private val context: Any?) {
    private val prefs = (context as Context).getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    actual fun getTheme(): String = prefs.getString("theme", "BLUE") ?: "BLUE"
    actual fun setTheme(value: String) { prefs.edit().putString("theme", value).apply() }
}
