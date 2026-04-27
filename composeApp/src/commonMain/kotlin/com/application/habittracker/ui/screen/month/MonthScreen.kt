package com.application.habittracker.ui.screen.month

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private val DAY_HEADERS = listOf("M", "T", "W", "T", "F", "S", "S")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<MonthViewModel>()
    val state by viewModel.uiState.collectAsState()
    val today = Instant
        .fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            MonthHeader(
                year = state.year,
                month = state.month,
                onPrev = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )
            Spacer(Modifier.height(16.dp))
            CalendarGrid(
                year = state.year,
                month = state.month,
                completionRatios = state.completionRatios,
                today = today
            )
            Spacer(Modifier.height(24.dp))
            Legend()
        }
    }
}

@Composable
private fun MonthHeader(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val monthName = monthName(month)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text("$monthName $year", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    completionRatios: Map<Int, Float>,
    today: LocalDate
) {
    val firstDayOffset = firstDayOfMonthOffset(year, month)
    val daysInMonth = daysInMonth(year, month)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOffset + 1
                    val isCurrentMonth = day in 1..daysInMonth
                    val isToday = isCurrentMonth &&
                            today.year == year && today.monthNumber == month && today.dayOfMonth == day

                    Box(modifier = Modifier.weight(1f)) {
                        if (isCurrentMonth) {
                            val ratio = completionRatios[day] ?: 0f
                            DayCell(
                                day = day,
                                ratio = ratio,
                                isToday = isToday,
                                baseColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, ratio: Float, isToday: Boolean, baseColor: Color) {
    val bgAlpha = if (ratio > 0f) ratio.coerceIn(0.15f, 1f) else 0f
    val bgColor = baseColor.copy(alpha = bgAlpha)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(
                if (isToday) Modifier.border(1.5.dp, baseColor, RoundedCornerShape(6.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (ratio > 0.5f) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Legend() {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Less", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(0.15f, 0.35f, 0.6f, 0.8f, 1f).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(primary.copy(alpha = alpha))
            )
        }
        Text("More", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun firstDayOfMonthOffset(year: Int, month: Int): Int {
    val firstDay = LocalDate(year, month, 1).dayOfWeek
    return when (firstDay) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
}

private fun daysInMonth(year: Int, month: Int): Int {
    val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    val firstOfThisMonth = LocalDate(year, month, 1)
    return (nextMonth.toEpochDays() - firstOfThisMonth.toEpochDays()).toInt()
}

private fun monthName(month: Int) = when (month) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
    else -> ""
}