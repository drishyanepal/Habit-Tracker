package com.application.habittracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.application.habittracker.di.appModule
import com.application.habittracker.nav.RouteMonthScreen
import com.application.habittracker.nav.RouteSettingsScreen
import com.application.habittracker.nav.RouteTodayScreen
import com.application.habittracker.theme.AppTheme
import com.application.habittracker.theme.MyThemeColor
import com.application.habittracker.ui.screen.month.MonthScreen
import com.application.habittracker.ui.screen.settings.SettingsScreen
import com.application.habittracker.ui.screen.today.TodayScreen
import org.koin.compose.KoinApplication

@Composable
fun App(
    context: Any? = null,
    darkTheme: Boolean,
    dynamicColor: Boolean,
) {
    var darkMode by remember { mutableStateOf(darkTheme) }
    var dynamicTheme by remember { mutableStateOf(dynamicColor) }

    KoinApplication(application = { modules(appModule(context)) }) {
        AppTheme(
            darkTheme = darkMode,
            selectedTheme = MyThemeColor.GREEN,
            dynamicColor = dynamicTheme && dynamicColor
        ) {
            val backStack = remember { mutableStateListOf<Any>(RouteTodayScreen) }
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is RouteTodayScreen -> NavEntry(key) {
                            TodayScreen(
                                onNavigateToMonth = { backStack.add(RouteMonthScreen) },
                                onNavigateToSettings = { backStack.add(RouteSettingsScreen) }
                            )
                        }

                        is RouteMonthScreen -> NavEntry(key) {
                            MonthScreen(onBack = { backStack.removeLastOrNull() })
                        }

                        is RouteSettingsScreen -> NavEntry(key) {
                            SettingsScreen(onBack = { backStack.removeLastOrNull() })
                        }

                        else -> NavEntry(Unit) { }
                    }
                }
            )
        }
    }
}