package com.application.habittracker.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.model.HabitWithStatus
import com.application.habittracker.data.repository.HabitRepository
import com.application.habittracker.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class SortOption(val label: String) {
    Default("Default"),
    CompletedLast("Completed Last"),
    Alphabetical("Alphabetical")
}

enum class FilterOption(val label: String) {
    Completed("Completed"),
    Incomplete("Incomplete")
}

class TodayViewModel(
    private val repository: HabitRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _habits = MutableStateFlow<List<HabitWithStatus>>(emptyList())
    val habits: StateFlow<List<HabitWithStatus>> = _habits.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.Default)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _activeFilters = MutableStateFlow<Set<FilterOption>>(emptySet())
    val activeFilters: StateFlow<Set<FilterOption>> = _activeFilters.asStateFlow()

    val displayedHabits: StateFlow<List<HabitWithStatus>> =
        combine(_habits, _sortOption, _activeFilters) { habits, sort, filters ->
            var list = when (filters.firstOrNull()) {
                FilterOption.Completed -> habits.filter { it.isCompleted }
                FilterOption.Incomplete -> habits.filter { !it.isCompleted }
                null -> habits
            }

            list = when (sort) {
                SortOption.Default -> list
                SortOption.CompletedLast -> list.sortedWith(
                    compareBy({ it.isCompleted }, { it.habit.createdAt })
                )
                SortOption.Alphabetical -> list.sortedBy { it.habit.name.lowercase() }
            }

            list
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleFilter(filter: FilterOption) {
        _activeFilters.update { current ->
            if (filter in current) emptySet() else setOf(filter)
        }
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
    }
}
