package com.application.habittracker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.habittracker.data.preferences.AppPreferences
import com.application.habittracker.di.appModule
import com.application.habittracker.theme.AppTheme
import com.application.habittracker.theme.MyThemeColor
import com.application.habittracker.ui.screen.month.MonthScreen
import com.application.habittracker.ui.screen.settings.SettingsScreen
import com.application.habittracker.ui.screen.today.TodayScreen
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Habits",     Icons.Default.Menu),
    TabItem("Statistics", Icons.Default.BarChart),
    TabItem("Settings",   Icons.Default.Settings),
)

@Composable
fun App(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    context: Any? = null,
) {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(appModule(context)) }),
        content = { AppContent(darkTheme, dynamicColor) }
    )
}

@Composable
private fun AppContent(darkTheme: Boolean, dynamicColor: Boolean) {
    val prefs = koinInject<AppPreferences>()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedTheme by remember {
        mutableStateOf(
            runCatching { MyThemeColor.valueOf(prefs.getTheme()) }.getOrDefault(MyThemeColor.BLUE)
        )
    }

    AppTheme(
        darkTheme = darkTheme,
        selectedTheme = selectedTheme,
        dynamicColor = dynamicColor,
    ) {
        Scaffold(
            bottomBar = {
                AppBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        ) { padding ->
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 250),
                modifier = Modifier.padding(padding),
            ) { tab ->
                when (tab) {
                    0 -> TodayScreen(
                        onNavigateToMonth = { selectedTab = 1 },
                        onNavigateToSettings = { selectedTab = 2 },
                    )
                    1 -> MonthScreen()
                    2 -> SettingsScreen(
                        selectedTheme = selectedTheme,
                        onThemeChange = { theme ->
                            selectedTheme = theme
                            prefs.setTheme(theme.name)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                BottomTabItem(
                    tab = tab,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(tab: TabItem, selected: Boolean, onClick: () -> Unit) {
    val pillColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceVariant
                      else MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "pill",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(250),
        label = "content",
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Capsule pill — wraps only the icon
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(pillColor)
                .padding(horizontal = 20.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
        )
    }
}
