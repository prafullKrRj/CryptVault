package com.cryptvault.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

data class CryptVaultMotion(
    val emphasized: CubicBezierEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    val emphasizedDecelerate: CubicBezierEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
    val emphasizedAccelerate: CubicBezierEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
    val standard: CubicBezierEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
)

val LocalCryptVaultMotion = compositionLocalOf<CryptVaultMotion> {
    error("CryptVaultMotion not provided")
}

@Composable
fun ProvideMotion(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCryptVaultMotion provides CryptVaultMotion(), content = content)
}