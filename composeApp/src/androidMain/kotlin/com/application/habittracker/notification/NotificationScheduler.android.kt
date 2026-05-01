package com.application.habittracker.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.application.habittracker.data.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

actual class NotificationScheduler actual constructor(context: Any?) {

    private val appContext: Context = (context as Context).applicationContext

    init {
        ensureChannel()
    }

    actual fun schedule(habit: Habit) {
        val time = habit.reminderTime ?: return
        val triggerAt = nextTriggerMillis(time)
        val intent = Intent(appContext, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habit.name)
        }
        val pi = PendingIntent.getBroadcast(
            appContext,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }

    actual fun cancel(habitId: Long) {
        val intent = Intent(appContext, HabitReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            appContext,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi)
    }

    actual fun rescheduleAll(habits: List<Habit>) {
        habits.forEach { habit ->
            if (habit.reminderTime != null) schedule(habit) else cancel(habit.id)
        }
    }

    actual fun requestPermission() {
        // Runtime permission handled in MainActivity for API 33+.
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Habit reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily reminders for your habits"
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun nextTriggerMillis(time: LocalTime): Long {
        val tz = TimeZone.currentSystemDefault()
        val nowInstant = Instant.fromEpochMilliseconds(
            kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
        val nowLdt = nowInstant.toLocalDateTime(tz)
        val today = nowLdt.date
        val candidate = LocalDateTime(today, time).toInstant(tz)
        val target = if (candidate.toEpochMilliseconds() <= nowInstant.toEpochMilliseconds()) {
            val nextDate = today.plus(DatePeriod(days = 1))
            LocalDateTime(nextDate, time).toInstant(tz)
        } else candidate
        return target.toEpochMilliseconds()
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }
}
