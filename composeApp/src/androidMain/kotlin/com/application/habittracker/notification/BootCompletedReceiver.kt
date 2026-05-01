package com.application.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.application.habittracker.data.db.HabitDatabase
import com.application.habittracker.data.repository.HabitRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val driver = AndroidSqliteDriver(HabitDatabase.Schema, context.applicationContext, "habit.db")
                val repo = HabitRepositoryImpl(HabitDatabase(driver))
                val habits = repo.getAllHabitsOnce()
                NotificationScheduler(context.applicationContext).rescheduleAll(habits)
            } finally {
                pending.finish()
            }
        }
    }
}
