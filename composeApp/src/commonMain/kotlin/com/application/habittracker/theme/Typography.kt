package com.application.habittracker.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.poppins_regular
import habittracker.composeapp.generated.resources.poppins_medium
import habittracker.composeapp.generated.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun poppinsFontFamily() = FontFamily(
    Font(Res.font.poppins_regular, weight = FontWeight.Normal),
    Font(Res.font.poppins_medium, weight = FontWeight.Medium),
    Font(Res.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.poppins_regular, weight = FontWeight.Bold),
)

@Composable
fun AppTypography(): Typography {
    val poppins = poppinsFontFamily()
    return Typography(
        displayLarge   = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold,    fontSize = 57.sp, lineHeight = 64.sp),
        displayMedium  = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold,    fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall   = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold,    fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge  = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall  = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge     = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium    = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall     = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge      = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium     = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall      = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge     = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium    = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall     = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp),
    )
}
