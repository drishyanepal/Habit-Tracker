package com.application.habittracker.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Habit(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val iconIndex: Int,
    val createdAt: LocalDate,
    val reminderTime: LocalTime? = null
)

data class HabitWithStatus(
    val habit: Habit,
    val isCompleted: Boolean
)
