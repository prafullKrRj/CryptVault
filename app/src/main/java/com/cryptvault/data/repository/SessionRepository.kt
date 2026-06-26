package com.cryptvault.data.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cryptvault.data.crypto.KeystoreManager
import com.cryptvault.data.crypto.PasswordDerivation
import com.cryptvault.data.prefs.SecurePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

enum class AppLifecyclePhase { Foreground, Background }

class SessionRepository(
    private val prefs: SecurePrefs,
) {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlockedFlow: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _lifecycle = MutableSharedFlow<AppLifecyclePhase>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val appLifecycle: SharedFlow<AppLifecyclePhase> = _lifecycle.asSharedFlow()

    @Volatile private var derivedKey: SecretKey? = null
    @Volatile private var salt: ByteArray? = null
    @Volatile private var verifier: ByteArray? = null

    val isUnlocked: Boolean get() = _isUnlocked.value

    val isInitialized: Boolean get() = prefs.isInitialized()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                _lifecycle.tryEmit(AppLifecyclePhase.Background)
            }
            override fun onStart(owner: LifecycleOwner) {
                _lifecycle.tryEmit(AppLifecyclePhase.Foreground)
            }
        })
    }

    fun autoLockSeconds(): Int = prefs.autoLockSeconds

    suspend fun setupMasterPassword(password: CharArray): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            require(password.size >= 4) { "Master password must be at least 4 characters" }
            val s = PasswordDerivation.generateSalt()
            val derived = PasswordDerivation.derive(password, s)
            val v = PasswordDerivation.derive(password, s, iterations = 32_000)
            prefs.writeMasterMaterial(s, v)
            derivedKey = SecretKeySpec(derived, "AES")
            salt = s
            verifier = v
            _isUnlocked.value = true
            clearChars(password)
        }
    }

    suspend fun unlockWithPassword(password: CharArray): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val s = prefs.readSalt() ?: error("Vault not initialized")
            val v = prefs.readVerifier() ?: error("Vault not initialized")
            val attempt = PasswordDerivation.derive(password, s, iterations = 32_000)
            if (!PasswordDerivation.constantTimeEquals(attempt, v)) {
                clearChars(password)
                error("Wrong master password")
            }
            val derived = PasswordDerivation.derive(password, s)
            derivedKey = SecretKeySpec(derived, "AES")
            salt = s
            verifier = v
            _isUnlocked.value = true
            clearChars(password)
        }
    }

    fun markBiometricUnlocked() {
        if (salt == null) salt = prefs.readSalt()
        if (verifier == null) verifier = prefs.readVerifier()
        _isUnlocked.value = true
    }

    fun activeKey(): SecretKey? = derivedKey

    fun lock() {
        derivedKey = null
        salt = null
        verifier = null
        _isUnlocked.value = false
    }

    fun resetAll() {
        prefs.wipe()
        KeystoreManager.deleteMasterKey()
        lock()
    }

    private fun clearChars(a: CharArray) { java.util.Arrays.fill(a, '\u0000') }
}