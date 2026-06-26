package com.cryptvault.domain.usecase

import com.cryptvault.data.repository.VaultRepository
import com.cryptvault.domain.model.VaultEntry
import kotlinx.coroutines.flow.Flow

class GetEntriesUseCase(private val repo: VaultRepository) {
    operator fun invoke(): Flow<List<VaultEntry>> = repo.observeEntries()
}

class AddEntryUseCase(private val repo: VaultRepository) {
    suspend operator fun invoke(entry: VaultEntry): Long = repo.upsert(entry)
}

class UpdateEntryUseCase(private val repo: VaultRepository) {
    suspend operator fun invoke(entry: VaultEntry): Long = repo.upsert(entry)
}

class DeleteEntryUseCase(private val repo: VaultRepository) {
    suspend operator fun invoke(entry: VaultEntry) = repo.delete(entry)
}

class GetEntryUseCase(private val repo: VaultRepository) {
    suspend operator fun invoke(id: Long): VaultEntry? = repo.get(id)
}