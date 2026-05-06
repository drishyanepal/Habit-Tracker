package com.application.habittracker.ui.screen.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class MonthUiState(
    val year: Int,
    val month: Int,
    val completionRatios: Map<Int, Float> = emptyMap(),
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null,
    val weeklyPattern: List<Float> = List(7) { 0f },
    val totalCompletionsThisMonth: Int = 0,
    val completionRateThisMonth: Float = 0f,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
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

            val days = daysInMonth(state.year, state.month)
            val weeklyPattern = computeWeeklyPattern(ratios, state.year, state.month, days)

            val totalCompletions: Int
            val completionRate: Float
            if (state.selectedHabitId == null) {
                totalCompletions = ratios.count { it.value > 0f }
                completionRate = if (days > 0) ratios.values.sumOf { it.toDouble() }.toFloat() / days else 0f
            } else {
                totalCompletions = ratios.size
                completionRate = if (days > 0) ratios.size.toFloat() / days else 0f
            }

            val (currentStreak, bestStreak) = if (state.selectedHabitId != null) {
                repository.getHabitStreaks(state.selectedHabitId)
            } else {
                0 to 0
            }

            _uiState.value = _uiState.value.copy(
                completionRatios = ratios,
                weeklyPattern = weeklyPattern,
                totalCompletionsThisMonth = totalCompletions,
                completionRateThisMonth = completionRate,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
            )
        }
    }

    private fun computeWeeklyPattern(
        ratios: Map<Int, Float>,
        year: Int,
        month: Int,
        days: Int
    ): List<Float> {
        val sums = FloatArray(7) { 0f }
        val counts = IntArray(7) { 0 }
        for (day in 1..days) {
            val dow = LocalDate(year, month, day).dayOfWeek.toIndex()
            counts[dow]++
            sums[dow] += ratios[day] ?: 0f
        }
        return List(7) { i -> if (counts[i] > 0) sums[i] / counts[i] else 0f }
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        val next = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        return (next.toEpochDays() - LocalDate(year, month, 1).toEpochDays()).toInt()
    }
}

private fun DayOfWeek.toIndex() = when (this) {
    DayOfWeek.MONDAY -> 0
    DayOfWeek.TUESDAY -> 1
    DayOfWeek.WEDNESDAY -> 2
    DayOfWeek.THURSDAY -> 3
    DayOfWeek.FRIDAY -> 4
    DayOfWeek.SATURDAY -> 5
    DayOfWeek.SUNDAY -> 6
}
