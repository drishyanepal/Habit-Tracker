package com.application.habittracker.data.model

import kotlinx.datetime.LocalDate

data class Habit(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val iconIndex: Int,
    val createdAt: LocalDate
)

data class HabitWithStatus(
    val habit: Habit,
    val isCompleted: Boolean
)