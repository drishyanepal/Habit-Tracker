package com.application.habittracker.data.preferences

import platform.Foundation.NSUserDefaults

actual class AppPreferences actual constructor(private val context: Any?) {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getTheme(): String = defaults.stringForKey("theme") ?: "BLUE"
    actual fun setTheme(value: String) { defaults.setObject(value, forKey = "theme") }
}
