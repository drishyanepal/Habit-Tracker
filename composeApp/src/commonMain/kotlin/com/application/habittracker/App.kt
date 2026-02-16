package com.application.habittracker

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.application.habittracker.nav.RouteMonthScreen
import com.application.habittracker.nav.RouteTodayScreen
import com.application.habittracker.screen.MonthScreen
import com.application.habittracker.screen.TodayScreen
import com.application.habittracker.theme.AppTheme
import com.application.habittracker.theme.MyThemeColor

@Composable
fun App(
    context: Any? = null,
    darkTheme: Boolean,
    dynamicColor: Boolean,
) {
    var themeSelection by remember { mutableStateOf("GREEN") }
    var darkMode by remember { mutableStateOf(darkTheme) }
    var dynamicTheme by remember { mutableStateOf(dynamicColor) }

    val selectedTheme = when (themeSelection) {
        "GREEN" -> MyThemeColor.GREEN
        else -> MyThemeColor.GREEN
    }

    AppTheme(
        darkTheme = darkMode,
        selectedTheme = selectedTheme,
        dynamicColor = dynamicTheme && dynamicColor
    ) {
        // Your app starts here
        val backStack = remember { mutableStateListOf<Any>(RouteTodayScreen) }
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is RouteTodayScreen -> NavEntry(key) {
                        TodayScreen(
                            gotoMonthlyScreen = { backStack.add(RouteMonthScreen) }
                        )
                    }

                    is RouteMonthScreen -> NavEntry(key) {
                        MonthScreen(
                            gotoYearScreen = {
                                if (backStack.size > 1) {
                                    backStack.removeLastOrNull()
                                }
                            }
                        )
                    }

                    else -> NavEntry(Unit) { Text("Unknown route") }
                }
            }
        )
    }
}

@Composable
fun Greeting() {
    Text("LKDFJLSDKJF")
}