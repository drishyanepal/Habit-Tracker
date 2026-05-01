package com.application.habittracker.data.preferences

expect class AppPreferences(context: Any?) {
    fun getTheme(): String
    fun setTheme(value: String)
}
