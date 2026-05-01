package com.application.habittracker.ui.screen.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val completionRatios: Map<Int, Float> = emptyMap()
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

    fun refresh() {
        loadCurrentMonth()
    }

    private fun loadCurrentMonth() {
        val state = _uiState.value
        viewModelScope.launch {
            val ratios = repository.getMonthCompletionRatios(state.year, state.month)
            _uiState.value = _uiState.value.copy(completionRatios = ratios)
        }
    }
}