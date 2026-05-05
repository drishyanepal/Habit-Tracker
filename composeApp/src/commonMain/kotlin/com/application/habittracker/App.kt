package com.application.habittracker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
