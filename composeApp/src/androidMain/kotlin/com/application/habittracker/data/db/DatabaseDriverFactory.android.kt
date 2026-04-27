package com.application.habittracker.data.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.application.habittracker.data.db.HabitDatabase

actual class DatabaseDriverFactory actual constructor(private val context: Any?) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(HabitDatabase.Schema, context as Context, "habit.db")
    }
}
