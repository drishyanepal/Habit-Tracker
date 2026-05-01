package com.application.habittracker.notification

import com.application.habittracker.data.model.Habit
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationScheduler actual constructor(context: Any?) {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun schedule(habit: Habit) {
        val time = habit.reminderTime ?: return
        val content = UNMutableNotificationContent().apply {
            setTitle("Time for ${habit.name}")
            setBody("Don't break the streak — let's do it!")
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            hour = time.hour.toLong()
            minute = time.minute.toLong()
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = true
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = habit.id.toString(),
            content = content,
            trigger = trigger
        )
        center.addNotificationRequest(request) { _ -> }
    }

    actual fun cancel(habitId: Long) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(habitId.toString()))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(habitId.toString()))
    }

    actual fun rescheduleAll(habits: List<Habit>) {
        habits.forEach { habit ->
            cancel(habit.id)
            if (habit.reminderTime != null) schedule(habit)
        }
    }

    actual fun requestPermission() {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        center.requestAuthorizationWithOptions(options) { _, _ -> }
    }
}
