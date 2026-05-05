package com.application.habittracker.ui.screen.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class MonthUiState(
    val year: Int,
    val month: Int,
    val completionRatios: Map<Int, Float> = emptyMap(),
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null
)

class MonthViewModel(private val repository: HabitRepository) : ViewModel() {

    private val now: LocalDateTime = Instant
        .fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault())

    private val _uiState = MutableStateFlow(
        MonthUiState(year = now.year, month = now.monthNumber)
    )
    val uiState: StateFlow<MonthUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
        loadCurrentMonth()
    }

    fun previousMonth() {
        val state = _uiState.value
        val (year, month) = if (state.month == 1) state.year - 1 to 12 else state.year to state.month - 1
        _uiState.value = _uiState.value.copy(year = year, month = month)
        loadCurrentMonth()
    }

    fun nextMonth() {
        val state = _uiState.value
        val (year, month) = if (state.month == 12) state.year + 1 to 1 else state.year to state.month + 1
        _uiState.value = _uiState.value.copy(year = year, month = month)
        loadCurrentMonth()
    }

    fun selectHabit(habitId: Long?) {
        _uiState.value = _uiState.value.copy(selectedHabitId = habitId)
        loadCurrentMonth()
    }

    fun refresh() {
        loadHabits()
        loadCurrentMonth()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            val habits = repository.getAllHabitsOnce()
            _uiState.value = _uiState.value.copy(habits = habits)
        }
    }

    private fun loadCurrentMonth() {
        val state = _uiState.value
        viewModelScope.launch {
            val ratios = if (state.selectedHabitId == null) {
                repository.getMonthCompletionRatios(state.year, state.month)
            } else {
                val completedDays = repository.getHabitMonthCompletions(
                    state.selectedHabitId, state.year, state.month
                )
                completedDays.associateWith { 1f }
            }
            _uiState.value = _uiState.value.copy(completionRatios = ratios)
        }
    }
}
