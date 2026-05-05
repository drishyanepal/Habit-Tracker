package com.application.habittracker.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.application.habittracker.MainActivity
import com.application.habittracker.R
import com.application.habittracker.data.preferences.AppPreferences
import kotlinx.datetime.LocalTime

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: return
        if (habitId < 0) return

        val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME)
            ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

        val prefs = context.getSharedPreferences(
            AppPreferences.PREFS_NAME, Context.MODE_PRIVATE
        )
        val notificationsOn = prefs.getBoolean(AppPreferences.KEY_NOTIFICATIONS_ENABLED, true)

        if (notificationsOn && reminderTime != null) {
            NotificationScheduler.scheduleExact(
                context.applicationContext,
                habitId,
                habitName,
                reminderTime,
                NotificationScheduler.nextTriggerMillisFor(reminderTime),
            )
        }

        if (!notificationsOn) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for $habitName")
            .setContentText("Don't break the streak — let's do it!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(habitId.toInt(), notification)
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_REMINDER_TIME = "reminder_time"
    }
}
