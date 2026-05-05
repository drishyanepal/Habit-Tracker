package com.application.habittracker.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.application.habittracker.MainActivity
import com.application.habittracker.R
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.preferences.AppPreferences
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

actual class NotificationScheduler actual constructor(
    context: Any?,
    private val prefs: AppPreferences,
) {

    private val appContext: Context = (context as Context).applicationContext

    init {
        ensureChannel()
    }

    actual fun schedule(habit: Habit) {
        if (!prefs.getNotificationsEnabled()) return
        val time = habit.reminderTime ?: return
        scheduleExact(appContext, habit.id, habit.name, time, nextTriggerMillis(time))
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

    actual fun sendTestNotification() {
        if (!prefs.getNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val openAppIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            appContext,
            TEST_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Test notification")
            .setContentText("Notifications are working — you're all set!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .build()
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(TEST_NOTIFICATION_ID, notification)
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

    private fun nextTriggerMillis(time: LocalTime): Long = nextTriggerMillisFor(time)

    companion object {
        const val CHANNEL_ID = "habit_reminders"
        private const val TEST_NOTIFICATION_ID = -1001

        fun scheduleExact(
            context: Context,
            habitId: Long,
            habitName: String,
            time: LocalTime,
            triggerAtMillis: Long,
        ) {
            val intent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
                putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
                putExtra(HabitReminderReceiver.EXTRA_REMINDER_TIME, time.toString())
            }
            val pi = PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                am.canScheduleExactAlarms()
            } else true
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        }

        fun nextTriggerMillisFor(time: LocalTime): Long {
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
    }
}
