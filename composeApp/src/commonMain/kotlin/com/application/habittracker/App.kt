package com.application.habittracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.application.habittracker.theme.AppTheme
import com.application.habittracker.theme.MyThemeColor
import org.jetbrains.compose.resources.painterResource

import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.compose_multiplatform

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
    }
}