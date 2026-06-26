package com.cryptvault.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptvault.data.prefs.SecurePrefs
import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.domain.usecase.LockVaultUseCase
import com.cryptvault.domain.usecase.WipeVaultUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val clipboardTtl: Int = 30,
    val autoLock: Int = 60,
    val biometricEnabled: Boolean = false,
    val message: String? = null,
    val confirmWipe: Boolean = false,
)

class SettingsViewModel(
    private val prefs: SecurePrefs,
    private val session: SessionRepository,
    private val lock: LockVaultUseCase,
    private val wipe: WipeVaultUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            clipboardTtl = prefs.clipboardTtlSeconds,
            autoLock = prefs.autoLockSeconds,
            biometricEnabled = prefs.biometricEnabled,
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun setClipboardTtl(value: Int) {
        prefs.clipboardTtlSeconds = value
        _state.update { it.copy(clipboardTtl = value) }
    }

    fun setAutoLock(value: Int) {
        prefs.autoLockSeconds = value
        _state.update { it.copy(autoLock = value) }
    }

    fun setBiometricEnabled(value: Boolean) {
        prefs.biometricEnabled = value
        _state.update { it.copy(biometricEnabled = value) }
    }

    fun lockNow() {
        lock()
        _state.update { it.copy(message = "Vault locked") }
    }

    fun requestWipe() { _state.update { it.copy(confirmWipe = true) } }
    fun cancelWipe() { _state.update { it.copy(confirmWipe = false) } }

    fun confirmWipe() {
        viewModelScope.launch {
            wipe()
            _state.update { it.copy(confirmWipe = false, message = "Vault wiped") }
        }
    }

    fun consumeMessage() { _state.update { it.copy(message = null) } }
}