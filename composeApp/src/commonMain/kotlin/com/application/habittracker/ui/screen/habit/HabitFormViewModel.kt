package com.application.habittracker.ui.screen.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.repository.HabitRepository
import com.application.habittracker.notification.NotificationScheduler
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class HabitFormViewModel(
    private val repository: HabitRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    fun saveHabit(
        id: Long?,
        name: String,
        colorIndex: Int,
        iconIndex: Int,
        reminderTime: LocalTime?,
        description: String?,
        repeatDays: Set<Int>,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val trimmed = name.trim()
            val habitId: Long
            if (id == null) {
                habitId = repository.insertHabit(trimmed, colorIndex, iconIndex, reminderTime, description, repeatDays)
            } else {
                repository.updateHabit(id, trimmed, colorIndex, iconIndex, reminderTime, description, repeatDays)
                habitId = id
            }
            scheduler.cancel(habitId)
            if (reminderTime != null) {
                scheduler.schedule(
                    Habit(
                        id = habitId,
                        name = trimmed,
                        colorIndex = colorIndex,
                        iconIndex = iconIndex,
                        createdAt = LocalDate(1970, 1, 1),
                        reminderTime = reminderTime,
                        repeatDays = repeatDays
                    )
                )
            }
            onDone()
        }
    }

    fun deleteHabit(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            scheduler.cancel(id)
            onDone()
        }
    }
}
