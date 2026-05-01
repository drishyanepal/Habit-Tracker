package com.application.habittracker.notification

import com.application.habittracker.data.model.Habit

expect class NotificationScheduler(context: Any?) {
    fun schedule(habit: Habit)
    fun cancel(habitId: Long)
    fun rescheduleAll(habits: List<Habit>)
    fun requestPermission()
}
