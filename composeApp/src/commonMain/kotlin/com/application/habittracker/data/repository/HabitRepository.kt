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
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getAllHabitsOnce(): List<Habit>
    suspend fun insertHabit(name: String, colorIndex: Int, iconIndex: Int, reminderTime: LocalTime?, description: String?, repeatDays: Set<Int>): Long
    suspend fun updateHabit(id: Long, name: String, colorIndex: Int, iconIndex: Int, reminderTime: LocalTime?, description: String?, repeatDays: Set<Int>)
    suspend fun deleteHabit(id: Long)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate)
    suspend fun getHabitsWithStatusForDate(date: LocalDate): List<HabitWithStatus>
    suspend fun getMonthCompletionRatios(year: Int, month: Int): Map<Int, Float>
    suspend fun getHabitMonthCompletions(habitId: Long, year: Int, month: Int): Set<Int>
    suspend fun getHabitStreaks(habitId: Long): Pair<Int, Int>
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
        description: String?,
        repeatDays: Set<Int>
    ): Long = withContext(Dispatchers.Default) {
        val today = today()
        db.transactionWithResult {
            queries.insertHabit(
                name = name,
                color_index = colorIndex.toLong(),
                icon_index = iconIndex.toLong(),
                created_at = today,
                reminder_time = reminderTime?.toString(),
                description = description?.takeIf { it.isNotBlank() },
                repeat_days = repeatDays.toRepeatDaysString()
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
        description: String?,
        repeatDays: Set<Int>
    ) = withContext(Dispatchers.Default) {
        queries.updateHabit(
            name = name,
            color_index = colorIndex.toLong(),
            icon_index = iconIndex.toLong(),
            reminder_time = reminderTime?.toString(),
            description = description?.takeIf { it.isNotBlank() },
            repeat_days = repeatDays.toRepeatDaysString(),
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
            val dayOfWeek = date.dayOfWeek.isoDayNumber - 1 // 0=Mon..6=Sun
            habits
                .filter { entity ->
                    val days = parseRepeatDays(entity.repeat_days)
                    days.isEmpty() || dayOfWeek in days
                }
                .map { entity ->
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

    override suspend fun getHabitStreaks(habitId: Long): Pair<Int, Int> =
        withContext(Dispatchers.Default) {
            val allEpochDays = completionQueries.getAllCompletionDatesForHabit(habitId)
                .executeAsList()
                .mapNotNull { runCatching { LocalDate.parse(it).toEpochDays() }.getOrNull() }
                .toSortedSet()

            if (allEpochDays.isEmpty()) return@withContext 0 to 0

            val todayEpoch = LocalDate.parse(today()).toEpochDays()

            var currentStreak = 0
            var checkDay = todayEpoch
            while (allEpochDays.contains(checkDay)) { currentStreak++; checkDay-- }
            if (currentStreak == 0) {
                checkDay = todayEpoch - 1
                while (allEpochDays.contains(checkDay)) { currentStreak++; checkDay-- }
            }

            val sorted = allEpochDays.toList()
            var bestStreak = if (sorted.isEmpty()) 0 else 1
            var run = 1
            for (i in 1 until sorted.size) {
                run = if (sorted[i] == sorted[i - 1] + 1) run + 1 else 1
                if (run > bestStreak) bestStreak = run
            }

            currentStreak to bestStreak
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
    description = description,
    repeatDays = parseRepeatDays(repeat_days)
)

private fun parseRepeatDays(raw: String?): Set<Int> {
    if (raw.isNullOrBlank()) return emptySet()
    return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
}

private fun Set<Int>.toRepeatDaysString(): String? {
    if (isEmpty()) return null
    return sorted().joinToString(",")
}
