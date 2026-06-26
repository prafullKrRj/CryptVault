package com.cryptvault.ui.unlock

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cryptvault.data.prefs.SecurePrefs
import com.cryptvault.domain.usecase.SetupMasterPasswordUseCase
import com.cryptvault.ui.common.AuroraBackground
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SetupMasterScreen(onDone: () -> Unit) {
    val setup: SetupMasterPasswordUseCase = koinInject()
    val prefs: SecurePrefs = koinInject()
    val scope = rememberCoroutineScope()

    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var biometricEnabled by rememberSaveable { mutableStateOf(false) }

    val strength = remember(password) { scorePassword(password) }
    val animatedStrength by animateFloatAsState(
        targetValue = strength.first.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "strength",
    )
    val canSubmit = password.length >= 8 && password == confirm && !loading
    val auroraColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
    )

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
    ) {
        AuroraBackground(
            baseColor = auroraColors[0],
            accentColor = auroraColors[1],
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp),
                )
            }
            Text(
                "Create your master password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "CryptVault uses this password to encrypt your vault. It's never stored or sent anywhere — if you forget it, the vault must be reset.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Master password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                supportingText = { Text("At least 4 characters") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            )

            if (password.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(5) { i ->
                        val active = i <= strength.first
                        val a by animateFloatAsState(
                            targetValue = if (active) 1f else 0.18f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                            label = "seg$i",
                        )
                        val color = strengthColor(strength.first)
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            color.copy(alpha = 0.4f + a * 0.55f),
                                            color.copy(alpha = 0.4f + a * 0.55f),
                                        )
                                    )
                                )
                        )
                    }
                }
                Text(
                    strength.second,
                    style = MaterialTheme.typography.labelMedium,
                    color = strengthColor(strength.first),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it; error = null },
                label = { Text("Confirm password") },
                singleLine = true,
                isError = confirm.isNotEmpty() && password != confirm,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                supportingText = {
                    if (confirm.isNotEmpty() && password != confirm) Text("Passwords do not match")
                    else Text(" ")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            )

            BiometricToggleRow(
                checked = biometricEnabled,
                onChange = { biometricEnabled = it },
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    error = null
                    loading = true
                    scope.launch {
                        val result = setup(password.toCharArray())
                        loading = false
                        result.onSuccess {
                            prefs.biometricEnabled = biometricEnabled
                            onDone()
                        }.onFailure { error = it.message ?: "Failed to create vault" }
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(Icons.Outlined.Lock, null)
                Spacer(Modifier.width(8.dp))
                Text("Create vault", fontWeight = FontWeight.SemiBold)
            }

            Text(
                "Tip: use a long passphrase you can remember. Example: “tide-otter-quartz-91”.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BiometricToggleRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Fingerprint, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Enable biometric unlock", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Use fingerprint, face, or device PIN to unlock faster.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

private fun scorePassword(pw: String): Pair<Int, String> = when {
    pw.length < 8 -> 0 to "Too short"
    pw.length < 12 -> 1 to "Weak"
    pw.length < 16 -> 2 to "Fair"
    pw.length < 20 -> 3 to "Strong"
    else -> 4 to "Excellent"
}

@Composable
private fun strengthColor(score: Int) = when (score) {
    0, 1 -> MaterialTheme.colorScheme.error
    2 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}