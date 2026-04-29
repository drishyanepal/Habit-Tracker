package com.application.habittracker.ui.component

// Index: 0-9, 10-19, 20-29, 30-39
val HABIT_ICONS = listOf(
    "✅", "💧", "🏃", "📚", "🧘", "💊", "🍎", "🦷", "🛏️", "⏰",
    "🚿", "🎯", "✏️", "🎵", "💻", "🌟", "❤️", "🧠", "💪", "🌿",
    "🥗", "☕", "🏋️", "🚴", "🧹", "📝", "🎨", "🌙", "🌅", "🎮",
    "🔥", "⚡", "🏆", "💎", "🌈", "🦋", "🌸", "🍵", "🥤", "🎖️"
)

enum class TemplateCategory(val label: String) {
    Good("Good"), Health("Health"), Bad("Bad"), ToDo("To-Do")
}

data class HabitTemplate(
    val name: String,
    val iconIndex: Int,
    val colorIndex: Int
)

val HABIT_TEMPLATES: Map<TemplateCategory, List<HabitTemplate>> = mapOf(
    TemplateCategory.Good to listOf(
        HabitTemplate("Make your Bed", 8, 2),
        HabitTemplate("Drink Water", 1, 5),
        HabitTemplate("Take a Cold Shower", 10, 3),
        HabitTemplate("Take Vitamins", 5, 4),
        HabitTemplate("Wake Up on Time", 9, 1),
        HabitTemplate("Eat a Healthy Meal", 6, 2),
        HabitTemplate("Brush Your Teeth", 7, 3),
        HabitTemplate("Read a Book", 3, 3),
        HabitTemplate("Take a Shower", 10, 5),
        HabitTemplate("Go for a Walk", 2, 2),
    ),
    TemplateCategory.Health to listOf(
        HabitTemplate("Exercise", 22, 2),
        HabitTemplate("Meditate", 4, 4),
        HabitTemplate("Journal", 25, 1),
        HabitTemplate("Sleep Early", 27, 4),
        HabitTemplate("Drink 8 Glasses of Water", 1, 5),
        HabitTemplate("Eat Less Sugar", 19, 2),
        HabitTemplate("Yoga", 4, 3),
    ),
    TemplateCategory.Bad to listOf(
        HabitTemplate("No Smoking", 30, 0),
        HabitTemplate("No Social Media", 14, 1),
        HabitTemplate("No Alcohol", 11, 0),
        HabitTemplate("No Junk Food", 20, 2),
        HabitTemplate("Limit Caffeine", 21, 1),
    ),
    TemplateCategory.ToDo to listOf(
        HabitTemplate("Study", 3, 3),
        HabitTemplate("Work on Project", 14, 3),
        HabitTemplate("Practice Guitar", 13, 4),
        HabitTemplate("Learn a Language", 11, 1),
        HabitTemplate("Code for 30 Minutes", 14, 3),
        HabitTemplate("Plan Tomorrow", 25, 5),
    )
)
