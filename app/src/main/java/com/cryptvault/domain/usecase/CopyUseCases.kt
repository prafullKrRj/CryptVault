package com.cryptvault.domain.usecase

import com.cryptvault.data.repository.SecureClipboard
import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.data.repository.VaultRepository
import com.cryptvault.domain.model.VaultEntry

class CopyWithTTLUseCase(
    private val clipboard: SecureClipboard,
    private val vault: VaultRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(entry: VaultEntry) {
        if (!session.isUnlocked) error("Vault locked")
        clipboard.copy(entry.password, label = entry.title)
    }
}

class CopyTextWithTTLUseCase(
    private val clipboard: SecureClipboard,
) {
    operator fun invoke(text: String, label: String = "CryptVault") {
        clipboard.copy(text, label)
    }
}