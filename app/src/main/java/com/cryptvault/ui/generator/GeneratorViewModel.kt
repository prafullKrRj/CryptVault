package com.cryptvault.ui.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptvault.data.repository.SecureClipboard
import com.cryptvault.domain.model.GeneratorConfig
import com.cryptvault.domain.model.PasswordStrength
import com.cryptvault.domain.usecase.CopyTextWithTTLUseCase
import com.cryptvault.domain.usecase.GeneratePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneratorState(
    val config: GeneratorConfig = GeneratorConfig(),
    val current: String = "",
    val strength: PasswordStrength = PasswordStrength(0, "—", 0.0),
    val message: String? = null,
)

class GeneratorViewModel(
    private val generate: GeneratePasswordUseCase,
    private val copy: CopyTextWithTTLUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    init {
        regenerate()
    }

    fun setLength(value: Int) {
        _state.update { it.copy(config = it.config.copy(length = value.coerceIn(4, 128))) }
        regenerate()
    }

    fun toggleUpper() { _state.update { it.copy(config = it.config.copy(useUpper = !it.config.useUpper)) }; regenerate() }
    fun toggleLower() { _state.update { it.copy(config = it.config.copy(useLower = !it.config.useLower)) }; regenerate() }
    fun toggleDigits() { _state.update { it.copy(config = it.config.copy(useDigits = !it.config.useDigits)) }; regenerate() }
    fun toggleSymbols() { _state.update { it.copy(config = it.config.copy(useSymbols = !it.config.useSymbols)) }; regenerate() }
    fun toggleAmbiguous() { _state.update { it.copy(config = it.config.copy(excludeAmbiguous = !it.config.excludeAmbiguous)) }; regenerate() }

    fun regenerate() {
        try {
            val cfg = _state.value.config
            val pwd = generate(cfg)
            val s = generate.strength(cfg)
            _state.update { it.copy(current = pwd, strength = s) }
        } catch (t: Throwable) {
            _state.update { it.copy(current = "", strength = PasswordStrength(0, "Invalid", 0.0), message = t.message) }
        }
    }

    fun copyCurrent() {
        val text = _state.value.current
        if (text.isNotEmpty()) {
            copy(text, label = "Generated password")
            _state.update { it.copy(message = "Password copied — clears in 30s") }
        }
    }

    fun consumeMessage() { _state.update { it.copy(message = null) } }
}