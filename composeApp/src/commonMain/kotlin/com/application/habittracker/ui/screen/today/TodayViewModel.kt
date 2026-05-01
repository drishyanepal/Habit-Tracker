package com.application.habittracker.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.HabitWithStatus
import com.application.habittracker.data.repository.HabitRepository
import com.application.habittracker.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TodayViewModel(
    private val repository: HabitRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _habits = MutableStateFlow<List<HabitWithStatus>>(emptyList())
    val habits: StateFlow<List<HabitWithStatus>> = _habits.asStateFlow()

    val today: LocalDate = Instant
        .fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    private var rescheduledOnce = false

    init {
        scheduler.requestPermission()
        viewModelScope.launch {
            repository.getAllHabits().collect { habits ->
                refreshHabits()
                if (!rescheduledOnce) {
                    scheduler.rescheduleAll(habits)
                    rescheduledOnce = true
                }
            }
        }
    }

    private suspend fun refreshHabits() {
        _habits.value = repository.getHabitsWithStatusForDate(today)
    }

    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, today)
            refreshHabits()
        }
    }
}
