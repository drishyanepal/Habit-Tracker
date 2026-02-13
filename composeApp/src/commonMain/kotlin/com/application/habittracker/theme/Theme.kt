package com.application.habittracker.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.application.habittracker.theme.color_palettes.GreenColorBase

typealias lightScheme = ColorScheme
typealias darkScheme = ColorScheme

fun createColorSchemes(palette: MaterialThemeColorsPalette): Pair<lightScheme, darkScheme> {
    val lightColors = lightColorScheme(
        primary = palette.primaryLight,
        onPrimary = palette.onPrimaryLight,
        primaryContainer = palette.primaryContainerLight,
        onPrimaryContainer = palette.onPrimaryContainerLight,
        secondary = palette.secondaryLight,
        onSecondary = palette.onSecondaryLight,
        secondaryContainer = palette.secondaryContainerLight,
        onSecondaryContainer = palette.onSecondaryContainerLight,
        tertiary = palette.tertiaryLight,
        onTertiary = palette.onTertiaryLight,
        tertiaryContainer = palette.tertiaryContainerLight,
        onTertiaryContainer = palette.onTertiaryContainerLight,
        error = palette.errorLight,
        onError = palette.onErrorLight,
        errorContainer = palette.errorContainerLight,
        onErrorContainer = palette.onErrorContainerLight,
        background = palette.backgroundLight,
        onBackground = palette.onBackgroundLight,
        surface = palette.surfaceLight,
        onSurface = palette.onSurfaceLight,
        surfaceVariant = palette.surfaceVariantLight,
        onSurfaceVariant = palette.onSurfaceVariantLight,
        outline = palette.outlineLight,
        outlineVariant = palette.outlineVariantLight,
        scrim = palette.scrimLight,
        inverseSurface = palette.inverseSurfaceLight,
        inverseOnSurface = palette.inverseOnSurfaceLight,
        inversePrimary = palette.inversePrimaryLight,
        surfaceDim = palette.surfaceDimLight,
        surfaceBright = palette.surfaceBrightLight,
        surfaceContainerLowest = palette.surfaceContainerLowestLight,
        surfaceContainerLow = palette.surfaceContainerLowLight,
        surfaceContainer = palette.surfaceContainerLight,
        surfaceContainerHigh = palette.surfaceContainerHighLight,
        surfaceContainerHighest = palette.surfaceContainerHighestLight,
    )

    val darkColors = darkColorScheme(
        primary = palette.primaryDark,
        onPrimary = palette.onPrimaryDark,
        primaryContainer = palette.primaryContainerDark,
        onPrimaryContainer = palette.onPrimaryContainerDark,
        secondary = palette.secondaryDark,
        onSecondary = palette.onSecondaryDark,
        secondaryContainer = palette.secondaryContainerDark,
        onSecondaryContainer = palette.onSecondaryContainerDark,
        tertiary = palette.tertiaryDark,
        onTertiary = palette.onTertiaryDark,
        tertiaryContainer = palette.tertiaryContainerDark,
        onTertiaryContainer = palette.onTertiaryContainerDark,
        error = palette.errorDark,
        onError = palette.onErrorDark,
        errorContainer = palette.errorContainerDark,
        onErrorContainer = palette.onErrorContainerDark,
        background = palette.backgroundDark,
        onBackground = palette.onBackgroundDark,
        surface = palette.surfaceDark,
        onSurface = palette.onSurfaceDark,
        surfaceVariant = palette.surfaceVariantDark,
        onSurfaceVariant = palette.onSurfaceVariantDark,
        outline = palette.outlineDark,
        outlineVariant = palette.outlineVariantDark,
        scrim = palette.scrimDark,
        inverseSurface = palette.inverseSurfaceDark,
        inverseOnSurface = palette.inverseOnSurfaceDark,
        inversePrimary = palette.inversePrimaryDark,
        surfaceDim = palette.surfaceDimDark,
        surfaceBright = palette.surfaceBrightDark,
        surfaceContainerLowest = palette.surfaceContainerLowestDark,
        surfaceContainerLow = palette.surfaceContainerLowDark,
        surfaceContainer = palette.surfaceContainerDark,
        surfaceContainerHigh = palette.surfaceContainerHighDark,
        surfaceContainerHighest = palette.surfaceContainerHighestDark,
    )

    return Pair(lightColors, darkColors)
}

enum class MyThemeColor {
    PINK, YELLOW, GREEN, ORANGE, RED, NEO
}

fun getSelectedThemeColors(myThemeSelected: MyThemeColor) : Pair<lightScheme, darkScheme> {
    return when (myThemeSelected) {
        MyThemeColor.GREEN -> createColorSchemes(GreenColorBase)
        else -> createColorSchemes(GreenColorBase)
    }
}


@Composable
expect fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    selectedTheme: MyThemeColor,
    content: @Composable () -> Unit
)