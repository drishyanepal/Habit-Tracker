package com.application.habittracker.data.preferences

expect class AppPreferences(context: Any?) {
    fun getTheme(): String
    fun setTheme(value: String)
    fun getNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(value: Boolean)
}
