package com.application.habittracker.notification

import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.preferences.AppPreferences

expect class NotificationScheduler(context: Any?, prefs: AppPreferences) {
    fun schedule(habit: Habit)
    fun cancel(habitId: Long)
    fun rescheduleAll(habits: List<Habit>)
    fun requestPermission()
    fun sendTestNotification()
}
