package com.application.habittracker.ui.screen.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.model.HabitWithStatus
import com.application.habittracker.ui.component.HABIT_COLORS
import com.application.habittracker.ui.screen.habit.HabitFormSheet
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToMonth: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel = koinViewModel<TodayViewModel>()
    val habits by viewModel.habits.collectAsState()
    val today = viewModel.today

    var showFormSheet by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(today.formatFull()) },
                actions = {
                    IconButton(onClick = onNavigateToMonth) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingHabit = null
                showFormSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            ProgressSection(habits)
            Spacer(Modifier.height(16.dp))
            if (habits.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No habits yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(habits, key = { it.habit.id }) { habitWithStatus ->
                        HabitRow(
                            habitWithStatus = habitWithStatus,
                            onToggle = { viewModel.toggleCompletion(habitWithStatus.habit.id) },
                            onEdit = {
                                editingHabit = habitWithStatus.habit
                                showFormSheet = true
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showFormSheet) {
        HabitFormSheet(
            habit = editingHabit,
            onDismiss = {
                showFormSheet = false
                editingHabit = null
            }
        )
    }
}

@Composable
private fun ProgressSection(habits: List<HabitWithStatus>) {
    val done = habits.count { it.isCompleted }
    val total = habits.size
    val progress = if (total == 0) 0f else done.toFloat() / total

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Today's progress", style = MaterialTheme.typography.labelLarge)
            Text("$done / $total", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
    }
}

@Composable
private fun HabitRow(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val habit = habitWithStatus.habit
    val color = HABIT_COLORS.getOrElse(habit.colorIndex) { HABIT_COLORS[0] }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(
            checked = habitWithStatus.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = color)
        )
    }
}

private fun LocalDate.formatFull(): String {
    val day = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$day, $dayOfMonth $monthName $year"
}