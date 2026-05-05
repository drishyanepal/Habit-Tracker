package com.application.habittracker.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.habittracker.notification.NotificationScheduler
import com.application.habittracker.theme.MyThemeColor
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

private data class ThemeOption(val color: MyThemeColor, val swatch: Color, val label: String)

private fun currentClockLabel(): String {
    val instant = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
    val now = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = now.hour.toString().padStart(2, '0')
    val mm = now.minute.toString().padStart(2, '0')
    val ss = now.second.toString().padStart(2, '0')
    return "$hh:$mm:$ss"
}

private val THEME_OPTIONS = listOf(
    ThemeOption(MyThemeColor.BLUE,   Color(0xFF0057CC), "Blue"),
    ThemeOption(MyThemeColor.GREEN,  Color(0xFF4C662B), "Green"),
    ThemeOption(MyThemeColor.PINK,   Color(0xFF6750A4), "Purple"),
    ThemeOption(MyThemeColor.ORANGE, Color(0xFF8B5000), "Orange"),
    ThemeOption(MyThemeColor.RED,    Color(0xFF904A43), "Red"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedTheme: MyThemeColor,
    onThemeChange: (MyThemeColor) -> Unit,
) {
    val scheduler = koinInject<NotificationScheduler>()
    val prefs = koinInject<com.application.habittracker.data.preferences.AppPreferences>()
    var notificationsEnabled by remember { mutableStateOf(prefs.getNotificationsEnabled()) }
    var lastTestSentAt by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { SectionHeader("Appearance") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = "Theme Color",
                        subtitle = THEME_OPTIONS.find { it.color == selectedTheme }?.label ?: "Blue",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            THEME_OPTIONS.forEach { option ->
                                val selected = selectedTheme == option.color
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(option.swatch)
                                        .then(
                                            if (selected) Modifier.border(
                                                2.5.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                CircleShape
                                            ) else Modifier
                                        )
                                        .clickable { onThemeChange(option.color) }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader("Notifications") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationsEnabled)
                            "Habit reminders are on"
                        else
                            "All notifications are silenced",
                    ) {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                prefs.setNotificationsEnabled(it)
                                if (!it) lastTestSentAt = null
                            }
                        )
                    }
                    AnimatedVisibility(
                        visible = notificationsEnabled,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsRow(
                                icon = Icons.Default.NotificationsActive,
                                title = "Send Test Notification",
                                subtitle = lastTestSentAt?.let { "Last sent: $it" }
                                    ?: "Verify notifications are working",
                            ) {
                                TextButton(onClick = {
                                    scheduler.sendTestNotification()
                                    lastTestSentAt = currentClockLabel()
                                }) { Text("Send") }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader("About") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "Habit Tracker",
                    ) {
                        Text(
                            "1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}
