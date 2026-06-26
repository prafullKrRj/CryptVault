package com.cryptvault.domain.usecase

import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.data.repository.VaultRepository

class SetupMasterPasswordUseCase(private val session: SessionRepository) {
    suspend operator fun invoke(password: CharArray): Result<Unit> =
        session.setupMasterPassword(password)
}

class UnlockWithPasswordUseCase(private val session: SessionRepository) {
    suspend operator fun invoke(password: CharArray): Result<Unit> =
        session.unlockWithPassword(password)
}

class UnlockWithBiometricUseCase(private val session: SessionRepository) {
    operator fun invoke(): Result<Unit> = runCatching { session.markBiometricUnlocked() }
}

class LockVaultUseCase(private val session: SessionRepository) {
    operator fun invoke() = session.lock()
}

class WipeVaultUseCase(
    private val session: SessionRepository,
    private val vault: VaultRepository,
) {
    suspend operator fun invoke() {
        vault.wipe()
        session.resetAll()
    }
}