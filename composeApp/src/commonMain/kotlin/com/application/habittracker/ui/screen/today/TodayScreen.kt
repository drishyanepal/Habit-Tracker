package com.application.habittracker.ui.screen.today

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.habittracker.data.model.Habit
import com.application.habittracker.data.model.HabitWithStatus
import com.application.habittracker.ui.component.HABIT_COLORS
import com.application.habittracker.ui.component.HABIT_ICONS
import com.application.habittracker.ui.screen.habit.HabitFormSheet
import com.application.habittracker.ui.util.rememberSoundPlayer
import kotlinx.datetime.LocalDate
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
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                editingHabit = null
                                showFormSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add habit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    val isCompleted = habitWithStatus.isCompleted
    val playCompletionSound = rememberSoundPlayer()
    val baseColor = HABIT_COLORS.getOrElse(habit.colorIndex) { HABIT_COLORS[0] }
    val lightColor = baseColor.toLightColor()
    val icon = HABIT_ICONS.getOrElse(habit.iconIndex) { "✅" }
    val cardShape = RoundedCornerShape(16.dp)

    // Animates from 0 (unchecked) to 1 (checked), drives the sliding fill
    val completionFraction by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "completion_slide"
    )

    val textColor = lerp(baseColor, Color.White, completionFraction)
    val subtitleColor = lerp(
        baseColor.copy(alpha = 0.72f),
        Color.White.copy(alpha = 0.85f),
        completionFraction
    )
    val iconBgColor = lerp(
        baseColor.copy(alpha = 0.18f),
        Color.White.copy(alpha = 0.22f),
        completionFraction
    )
    val buttonBorderColor = lerp(
        baseColor.copy(alpha = 0.85f),
        Color.White.copy(alpha = 0.75f),
        completionFraction
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = cardShape)
            .clip(cardShape)
            .drawBehind {
                // Pastel base always fills the full card
                drawRect(lightColor)
                // Bold color slides in from left as the habit is completed
                drawRect(baseColor, size = size.copy(width = size.width * completionFraction))
            }
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Habit emoji icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp)
        }

        // Name and frequency
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "Every day",
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }

        // Circular toggle: "+" unchecked → checkmark checked
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, buttonBorderColor, CircleShape)
                .background(Color.White.copy(alpha = 0.18f * completionFraction))
                .clickable {
                    if (!isCompleted) playCompletionSound()
                    onToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Mark done",
                    tint = baseColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// Blends the color towards white to produce a pastel background
private fun Color.toLightColor(factor: Float = 0.18f): Color = Color(
    red = 1f - (1f - red) * factor,
    green = 1f - (1f - green) * factor,
    blue = 1f - (1f - blue) * factor,
    alpha = 1f
)

private fun LocalDate.formatFull(): String {
    val day = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$day, $dayOfMonth $monthName $year"
}
