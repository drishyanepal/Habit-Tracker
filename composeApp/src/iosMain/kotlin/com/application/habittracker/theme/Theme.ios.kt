package com.application.habittracker.theme

import androidx.compose.runtime.Composable

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    selectedTheme: MyThemeColor,
    content: @Composable (() -> Unit)
) {
}