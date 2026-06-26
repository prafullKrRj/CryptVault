package com.cryptvault.ui.unlock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private val AuthAllowed = setOf(
    BiometricManager.Authenticators.BIOMETRIC_STRONG,
    BiometricManager.Authenticators.BIOMETRIC_WEAK,
    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
)

fun canUseBiometric(activity: FragmentActivity): BiometricAvailability {
    val manager = BiometricManager.from(activity)
    val combined = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val code = manager.canAuthenticate(combined)
    return when (code) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available(combined)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.Unavailable(code)
        else -> BiometricAvailability.Unavailable(code)
    }
}

sealed interface BiometricAvailability {
    data class Available(val authenticators: Int) : BiometricAvailability
    data class Unavailable(val code: Int) : BiometricAvailability
}

fun FragmentActivity.showBiometricPrompt(
    title: String,
    subtitle: String,
    negative: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(this)
    val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError(errString.toString())
        override fun onAuthenticationFailed() = onError("Try again")
    })
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negative)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()
    prompt.authenticate(info)
}