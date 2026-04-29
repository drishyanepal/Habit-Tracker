package com.application.habittracker.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    selectedTheme: MyThemeColor,
    content: @Composable (() -> Unit)
) {
    val (lightScheme, darkScheme) = getSelectedThemeColors(selectedTheme)
    val colorScheme = if (darkTheme) darkScheme else lightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content
    )
}