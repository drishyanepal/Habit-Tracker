package com.application.habittracker.ui.screen.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.application.habittracker.data.model.Habit
import com.application.habittracker.ui.component.HABIT_COLORS
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormSheet(
    habit: Habit? = null,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<HabitFormViewModel>()
    val sheetState = rememberModalBottomSheetState()

    var name by remember { mutableStateOf(habit?.name ?: "") }
    var selectedColor by remember { mutableIntStateOf(habit?.colorIndex ?: 0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (habit == null) "New habit" else "Edit habit",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (name.isNotBlank()) {
                        viewModel.saveHabit(habit?.id, name, selectedColor, onDismiss)
                    }
                })
            )

            Text("Color", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HABIT_COLORS.forEachIndexed { index, color ->
                    ColorDot(
                        color = color,
                        selected = index == selectedColor,
                        onClick = { selectedColor = index }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveHabit(habit?.id, name, selectedColor, onDismiss) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (habit != null) {
                if (showDeleteConfirm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delete this habit?", style = MaterialTheme.typography.bodyMedium)
                        Row {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                            TextButton(
                                onClick = { viewModel.deleteHabit(habit.id, onDismiss) }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                } else {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete habit")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}