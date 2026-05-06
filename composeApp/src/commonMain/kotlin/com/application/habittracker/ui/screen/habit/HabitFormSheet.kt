package com.application.habittracker.ui.screen.habit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.habittracker.data.model.Habit
import com.application.habittracker.ui.component.HABIT_COLORS
import com.application.habittracker.ui.component.HABIT_ICONS
import com.application.habittracker.ui.component.HABIT_TEMPLATES
import com.application.habittracker.ui.component.HabitTemplate
import com.application.habittracker.ui.component.TemplateCategory
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel

private enum class HabitFormStep { Templates, Form }

private val Green = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormSheet(
    habit: Habit? = null,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<HabitFormViewModel>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember {
        mutableStateOf(if (habit == null) HabitFormStep.Templates else HabitFormStep.Form)
    }
    var prefilledName by remember { mutableStateOf("") }
    var prefilledIconIndex by remember { mutableIntStateOf(0) }
    var prefilledColorIndex by remember { mutableIntStateOf(2) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            when (step) {
                HabitFormStep.Templates -> TemplatesContent(
                    onDismiss = onDismiss,
                    onCreateCustom = {
                        prefilledName = ""
                        prefilledIconIndex = 0
                        prefilledColorIndex = 2
                        step = HabitFormStep.Form
                    },
                    onSelectTemplate = { template ->
                        prefilledName = template.name
                        prefilledIconIndex = template.iconIndex
                        prefilledColorIndex = template.colorIndex
                        step = HabitFormStep.Form
                    }
                )

                HabitFormStep.Form -> CustomHabitForm(
                    habit = habit,
                    initialName = habit?.name ?: prefilledName,
                    initialIconIndex = habit?.iconIndex ?: prefilledIconIndex,
                    initialColorIndex = habit?.colorIndex ?: prefilledColorIndex,
                    initialReminderTime = habit?.reminderTime,
                    initialDescription = habit?.description,
                    initialRepeatDays = habit?.repeatDays ?: emptySet(),
                    onBack = if (habit == null) ({ step = HabitFormStep.Templates }) else onDismiss,
                    onSave = { name, colorIndex, iconIndex, reminderTime, description, repeatDays ->
                        viewModel.saveHabit(habit?.id, name, colorIndex, iconIndex, reminderTime, description, repeatDays, onDismiss)
                    },
                    onDelete = if (habit != null) ({
                        viewModel.deleteHabit(habit.id, onDismiss)
                    }) else null
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Templates screen
// ──────────────────────────────────────────────────────────────

@Composable
private fun TemplatesContent(
    onDismiss: () -> Unit,
    onCreateCustom: () -> Unit,
    onSelectTemplate: (HabitTemplate) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TemplateCategory.Good) }
    var searchQuery by remember { mutableStateOf("") }

    val templates = HABIT_TEMPLATES[selectedCategory] ?: emptyList()
    val filtered = if (searchQuery.isBlank()) templates
    else templates.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = "Templates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Tune, contentDescription = "Settings")
            }
        }

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(TemplateCategory.entries) { category ->
                val selected = category == selectedCategory
                Surface(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(50),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            // Create custom habit card
            item {
                CreateCustomHabitRow(onClick = onCreateCustom)
                Spacer(Modifier.height(20.dp))
            }

            // Most popular header
            item {
                Text(
                    text = "Most Popular",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Template rows
            items(filtered) { template ->
                TemplateRow(template = template, onClick = { onSelectTemplate(template) })
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 16.dp))
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun CreateCustomHabitRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Green),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Create a Custom Habit",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemplateRow(template: HabitTemplate, onClick: () -> Unit) {
    val color = HABIT_COLORS.getOrElse(template.colorIndex) { HABIT_COLORS[0] }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(HABIT_ICONS.getOrElse(template.iconIndex) { "✅" }, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = template.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Custom habit form
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomHabitForm(
    habit: Habit?,
    initialName: String,
    initialIconIndex: Int,
    initialColorIndex: Int,
    initialReminderTime: LocalTime?,
    initialDescription: String?,
    initialRepeatDays: Set<Int>,
    onBack: () -> Unit,
    onSave: (name: String, colorIndex: Int, iconIndex: Int, reminderTime: LocalTime?, description: String?, repeatDays: Set<Int>) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColorIndex by remember { mutableIntStateOf(initialColorIndex) }
    var selectedIconIndex by remember { mutableIntStateOf(initialIconIndex) }
    var reminderTime by remember { mutableStateOf(initialReminderTime) }
    var description by remember { mutableStateOf(initialDescription ?: "") }
    var repeatDays by remember { mutableStateOf(initialRepeatDays) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var descriptionDraft by remember { mutableStateOf(description) }
    val nameFocusRequester = remember { FocusRequester() }

    val selectedColor = HABIT_COLORS.getOrElse(selectedColorIndex) { HABIT_COLORS[2] }
    val selectedIcon = HABIT_ICONS.getOrElse(selectedIconIndex) { "✅" }
    val suggestions = listOf("Read", "Write", "Exercise", "Meditate", "Learn")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (habit == null) "Add Habit" else "Edit Habit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            if (onDelete != null) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (name.isNotBlank()) Green else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = name.isNotBlank()) {
                        onSave(name, selectedColorIndex, selectedIconIndex, reminderTime, description.takeIf { it.isNotBlank() }, repeatDays)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save",
                    tint = if (name.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Preview card with inline text input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedIcon,
                        fontSize = 30.sp,
                        modifier = Modifier.size(44.dp),
                        textAlign = TextAlign.Center
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { if (it.length <= 100) name = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color.White),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (name.isEmpty()) {
                                        Text(
                                            "Enter habit name...",
                                            style = TextStyle(
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nameFocusRequester)
                        )
                        val repeatLabel = if (repeatDays.isEmpty()) "Every day"
                        else {
                            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            repeatDays.sorted().joinToString(", ") { dayNames[it] }
                        }
                        Text(
                            repeatLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Suggestion chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestions) { suggestion ->
                    FilterChip(
                        selected = name.trim().equals(suggestion, ignoreCase = true),
                        onClick = { name = suggestion },
                        label = { Text(suggestion) }
                    )
                }
            }

            // Character counter
            Text(
                text = "${name.length}/100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            // Appearance section label
            Text(
                "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Appearance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                AppearanceRow(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE91E63)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    label = "Color",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(selectedColor)
                        )
                    },
                    onClick = { showColorPicker = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                AppearanceRow(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(selectedColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedIcon, fontSize = 18.sp)
                        }
                    },
                    label = "Icon",
                    trailing = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { showIconPicker = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                AppearanceRow(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF5722)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    label = "Description",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (description.isBlank()) "Empty" else description.take(20) + if (description.length > 20) "…" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { showDescriptionDialog = true }
                )
            }

            // Schedule section label
            Text(
                "Schedule",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Day-of-week picker
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        "Repeat",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(10.dp))
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        dayLabels.forEachIndexed { index, label ->
                            val effectiveDays = if (repeatDays.isEmpty()) (0..6).toSet() else repeatDays
                            val isSelected = index in effectiveDays
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) selectedColor
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        val newEffective = if (index in effectiveDays) effectiveDays - index else effectiveDays + index
                                        if (newEffective.isNotEmpty()) {
                                            repeatDays = if (newEffective.size == 7) emptySet() else newEffective
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Reminders section label
            Text(
                "Reminders",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Reminder card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                AppearanceRow(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3F51B5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    label = "Reminder time",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                reminderTime?.formatLabel() ?: "Off",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (reminderTime != null) {
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { reminderTime = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear reminder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = { showTimePicker = true }
                )
            }
        }
    }

    // Color picker dialog
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Select Color") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    itemsIndexed(HABIT_COLORS) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (index == selectedColorIndex)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable {
                                    selectedColorIndex = index
                                    showColorPicker = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) { Text("Done") }
            }
        )
    }

    // Icon picker dialog
    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text("Select Icon") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    itemsIndexed(HABIT_ICONS) { index, icon ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (index == selectedIconIndex)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedIconIndex = index
                                    showIconPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) { Text("Done") }
            }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        val initial = reminderTime ?: LocalTime(8, 0)
        val timePickerState = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = LocalTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    // Delete confirm dialog
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete habit?") },
            text = { Text("This will permanently delete this habit and all its completion history.") },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Description dialog
    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Description") },
            text = {
                OutlinedTextField(
                    value = descriptionDraft,
                    onValueChange = { if (it.length <= 300) descriptionDraft = it },
                    placeholder = { Text("Optional note about this habit…") },
                    supportingText = { Text("${descriptionDraft.length}/300") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    description = descriptionDraft
                    showDescriptionDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    descriptionDraft = description
                    showDescriptionDialog = false
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AppearanceRow(
    leadingIcon: @Composable () -> Unit,
    label: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        leadingIcon()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

private fun LocalTime.formatLabel(): String {
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val period = if (hour < 12) "AM" else "PM"
    val mm = minute.toString().padStart(2, '0')
    return "$h12:$mm $period"
}
