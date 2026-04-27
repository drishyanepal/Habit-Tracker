package com.application.habittracker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.application.habittracker.data.db.HabitDatabase

actual class DatabaseDriverFactory actual constructor(private val context: Any?) {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(HabitDatabase.Schema, "habit.db")
    }
}
