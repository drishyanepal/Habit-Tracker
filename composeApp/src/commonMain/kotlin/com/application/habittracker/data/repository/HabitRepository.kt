package com.application.habittracker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.application.habittracker.data.db.HabitDatabase
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.model.HabitWithStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getAllHabitsOnce(): List<Habit>
    suspend fun insertHabit(name: String, colorIndex: Int, iconIndex: Int, reminderTime: LocalTime?, description: String?): Long
    suspend fun updateHabit(id: Long, name: String, colorIndex: Int, iconIndex: Int, reminderTime: LocalTime?, description: String?)
    suspend fun deleteHabit(id: Long)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate)
    suspend fun getHabitsWithStatusForDate(date: LocalDate): List<HabitWithStatus>
    suspend fun getMonthCompletionRatios(year: Int, month: Int): Map<Int, Float>
    suspend fun getHabitMonthCompletions(habitId: Long, year: Int, month: Int): Set<Int>
}

class HabitRepositoryImpl(private val db: HabitDatabase) : HabitRepository {

    private val queries get() = db.habitQueries
    private val completionQueries get() = db.habitCompletionQueries

    override fun getAllHabits(): Flow<List<Habit>> =
        queries.getAllHabits().asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getAllHabitsOnce(): List<Habit> = withContext(Dispatchers.Default) {
        queries.getAllHabits().executeAsList().map { it.toDomain() }
    }

    override suspend fun insertHabit(
        name: String,
        colorIndex: Int,
        iconIndex: Int,
        reminderTime: LocalTime?,
        description: String?
    ): Long = withContext(Dispatchers.Default) {
        val today = today()
        db.transactionWithResult {
            queries.insertHabit(
                name = name,
                color_index = colorIndex.toLong(),
                icon_index = iconIndex.toLong(),
                created_at = today,
                reminder_time = reminderTime?.toString(),
                description = description?.takeIf { it.isNotBlank() }
            )
            queries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun updateHabit(
        id: Long,
        name: String,
        colorIndex: Int,
        iconIndex: Int,
        reminderTime: LocalTime?,
        description: String?
    ) = withContext(Dispatchers.Default) {
        queries.updateHabit(
            name = name,
            color_index = colorIndex.toLong(),
            icon_index = iconIndex.toLong(),
            reminder_time = reminderTime?.toString(),
            description = description?.takeIf { it.isNotBlank() },
            id = id
        )
        Unit
    }

    override suspend fun deleteHabit(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteHabit(id)
        Unit
    }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate) =
        withContext(Dispatchers.Default) {
            val dateStr = date.toString()
            val count = completionQueries.isCompleted(habitId, dateStr).executeAsOne()
            if (count > 0L) {
                completionQueries.deleteCompletion(habit_id = habitId, completed_date = dateStr)
            } else {
                completionQueries.insertCompletion(habit_id = habitId, completed_date = dateStr)
            }
            Unit
        }

    override suspend fun getHabitsWithStatusForDate(date: LocalDate): List<HabitWithStatus> =
        withContext(Dispatchers.Default) {
            val habits = queries.getAllHabits().executeAsList()
            val completedIds = completionQueries
                .getCompletedHabitIdsForDate(date.toString())
                .executeAsList().toSet()
            habits.map { entity ->
                HabitWithStatus(
                    habit = entity.toDomain(),
                    isCompleted = entity.id in completedIds
                )
            }
        }

    override suspend fun getMonthCompletionRatios(year: Int, month: Int): Map<Int, Float> =
        withContext(Dispatchers.Default) {
            val totalHabits = queries.getAllHabits().executeAsList().size
            if (totalHabits == 0) return@withContext emptyMap()
            val monthPrefix = "$year-${month.toString().padStart(2, '0')}%"
            val counts = completionQueries.getCompletionCountsForMonth(monthPrefix).executeAsList()
            counts.associate { row ->
                val day = row.completed_date.substringAfterLast('-').toIntOrNull() ?: 0
                day to (row.count.toFloat() / totalHabits).coerceIn(0f, 1f)
            }
        }

    override suspend fun getHabitMonthCompletions(habitId: Long, year: Int, month: Int): Set<Int> =
        withContext(Dispatchers.Default) {
            val monthPrefix = "$year-${month.toString().padStart(2, '0')}%"
            completionQueries.getCompletionDatesForHabitInMonth(habitId, monthPrefix)
                .executeAsList()
                .mapNotNull { it.substringAfterLast('-').toIntOrNull() }
                .toSet()
        }

    private fun today(): String {
        val ms = kotlin.time.Clock.System.now().toEpochMilliseconds()
        return Instant.fromEpochMilliseconds(ms)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
}

private fun com.application.habittracker.data.db.Habit.toDomain(): Habit = Habit(
    id = id,
    name = name,
    colorIndex = color_index.toInt(),
    iconIndex = icon_index.toInt(),
    createdAt = LocalDate.parse(created_at),
    reminderTime = reminder_time?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
    description = description
)
