package com.cryptvault.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val display = TextStyle(fontFamily = FontFamily.Default)
private val body = TextStyle(fontFamily = FontFamily.Default)

val CryptVaultTypography = Typography(
    displayLarge = display.copy(fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp, fontWeight = FontWeight.Normal),
    displayMedium = display.copy(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Normal),
    displaySmall = display.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Normal),
    headlineLarge = display.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = display.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = display.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    titleMedium = body.copy(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp, fontWeight = FontWeight.Medium),
    titleSmall = body.copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, fontWeight = FontWeight.Medium),
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Normal),
    bodyMedium = body.copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp, fontWeight = FontWeight.Normal),
    bodySmall = body.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp, fontWeight = FontWeight.Normal),
    labelLarge = body.copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, fontWeight = FontWeight.Medium),
    labelMedium = body.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium),
    labelSmall = body.copy(fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium),
)