package com.cryptvault.ui.unlock

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.cryptvault.data.prefs.SecurePrefs
import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.domain.usecase.UnlockWithBiometricUseCase
import com.cryptvault.domain.usecase.UnlockWithPasswordUseCase
import com.cryptvault.ui.common.AuroraBackground
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val activity = LocalContextAsFragment()
    val session: SessionRepository = koinInject()
    val prefs: SecurePrefs = koinInject()
    val unlockPw: UnlockWithPasswordUseCase = koinInject()
    val unlockBio: UnlockWithBiometricUseCase = koinInject()
    val scope = rememberCoroutineScope()

    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var biometricAvailable by remember { mutableStateOf<BiometricAvailability?>(null) }

    LaunchedEffect(activity) {
        biometricAvailable = canUseBiometric(activity)
        val auto = biometricAvailable is BiometricAvailability.Available && prefs.biometricEnabled
        if (auto) {
            promptBiometric(
                activity = activity,
                onSuccess = {
                    unlockBio()
                    onUnlocked()
                },
                onError = { /* user can still type password */ },
            )
        }
    }

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
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            PulsingShield()
            Spacer(Modifier.height(8.dp))
            Text(
                "CryptVault",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Unlock your vault",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Master password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    error = null
                    loading = true
                    scope.launch {
                        val result = unlockPw(password.toCharArray())
                        loading = false
                        result.onSuccess { onUnlocked() }
                            .onFailure { error = it.message ?: "Unlock failed" }
                    }
                },
                enabled = password.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text(if (loading) "Unlocking…" else "Unlock", style = MaterialTheme.typography.titleMedium)
            }

            if (biometricAvailable is BiometricAvailability.Available && prefs.biometricEnabled) {
                OutlinedButton(
                    onClick = {
                        promptBiometric(
                            activity = activity,
                            onSuccess = { unlockBio(); onUnlocked() },
                            onError = { error = it },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Icon(Icons.Outlined.Fingerprint, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use biometrics")
                }
            }

            Spacer(Modifier.weight(1f))

            TextButton(onClick = onForgotPassword) {
                Text("Forgot password? Reset vault")
            }
        }
    }
}

@Composable
private fun PulsingShield() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val glow by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer { scaleX = pulse; scaleY = pulse; alpha = 0.55f + glow * 0.45f },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    rotationZ = -8f
                    shadowElevation = 24f
                    shape = androidx.compose.foundation.shape.CircleShape
                    clip = true
                }
                .padding(0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp),
            )
        }
    }
}

@Composable
private fun LocalContextAsFragment(): FragmentActivity {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var cur = ctx
    while (cur is android.content.ContextWrapper) {
        if (cur is FragmentActivity) return cur
        cur = cur.baseContext
    }
    error("CryptVault requires a FragmentActivity context")
}

private fun promptBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    activity.showBiometricPrompt(
        title = "Unlock CryptVault",
        subtitle = "Confirm it's you",
        negative = "Use password",
        onSuccess = onSuccess,
        onError = onError,
    )
}

@Suppress("unused")
private fun rotate(scale: Float): Modifier = Modifier.graphicsLayer { rotationZ = scale }