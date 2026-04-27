package com.application.habittracker.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.HabitWithStatus
import com.application.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TodayViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _habits = MutableStateFlow<List<HabitWithStatus>>(emptyList())
    val habits: StateFlow<List<HabitWithStatus>> = _habits.asStateFlow()

    val today: LocalDate = Instant
        .fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    init {
        viewModelScope.launch {
            repository.getAllHabits().collect {
                refreshHabits()
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