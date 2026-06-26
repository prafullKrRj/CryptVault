package com.cryptvault.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class EntryCategory(val label: String, val icon: ImageVector, val tint: Color) {
    Login("Login", Icons.Outlined.Key, Color(0xFF4F46E5)),
    Email("Email", Icons.Outlined.Email, Color(0xFF0EA5E9)),
    Card("Card", Icons.Outlined.CreditCard, Color(0xFFDC2626)),
    Wifi("Wi-Fi", Icons.Outlined.Wifi, Color(0xFF7C3AED)),
    Note("Note", Icons.Outlined.NoteAlt, Color(0xFF059669)),
    Other("Other", Icons.Outlined.Lock, Color(0xFF6B7280)),
}

data class VaultEntry(
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val category: EntryCategory = EntryCategory.Login,
    val createdAt: Long,
    val updatedAt: Long,
)

data class GeneratorConfig(
    val length: Int = 20,
    val useUpper: Boolean = true,
    val useLower: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
)

data class PasswordStrength(
    val score: Int,
    val label: String,
    val entropyBits: Double,
)

enum class StrengthBucket(val label: String) {
    VeryWeak("Very Weak"),
    Weak("Weak"),
    Fair("Fair"),
    Strong("Strong"),
    Excellent("Excellent"),
}