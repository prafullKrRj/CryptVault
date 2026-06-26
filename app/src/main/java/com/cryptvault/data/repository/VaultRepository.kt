package com.cryptvault.data.repository

import com.cryptvault.data.crypto.CipherEngine
import com.cryptvault.data.crypto.KeystoreManager
import com.cryptvault.data.db.VaultDao
import com.cryptvault.data.db.VaultEntryEntity
import com.cryptvault.domain.model.EntryCategory
import com.cryptvault.domain.model.VaultEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val dao: VaultDao,
) {

    private val masterKey by lazy { KeystoreManager.getOrCreateMasterKey() }

    fun observeEntries(): Flow<List<VaultEntry>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun get(id: Long): VaultEntry? = dao.getById(id)?.toDomain()

    suspend fun upsert(entry: VaultEntry): Long {
        val now = System.currentTimeMillis()
        val entity = entry.toEntity(now)
        return if (entry.id == 0L) {
            dao.insert(entity.copy(createdAt = now, updatedAt = now))
        } else {
            dao.update(entity)
            entry.id
        }
    }

    suspend fun delete(entry: VaultEntry) {
        val entity = entry.toEntity(System.currentTimeMillis())
        dao.delete(entity)
    }

    suspend fun wipe() {
        dao.deleteAll()
    }

    suspend fun count(): Int = dao.count()

    private fun VaultEntryEntity.toDomain(): VaultEntry = VaultEntry(
        id = id,
        title = title,
        username = username,
        password = CipherEngine.decrypt(masterKey, passwordBlob).toString(Charsets.UTF_8),
        notes = notesBlob?.let { CipherEngine.decrypt(masterKey, it).toString(Charsets.UTF_8) }.orEmpty(),
        category = runCatching { EntryCategory.valueOf(category) }.getOrDefault(EntryCategory.Login),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun VaultEntry.toEntity(now: Long): VaultEntryEntity = VaultEntryEntity(
        id = id,
        title = title,
        username = username,
        passwordBlob = CipherEngine.encrypt(masterKey, password.toByteArray(Charsets.UTF_8)),
        notesBlob = notes.takeIf { it.isNotEmpty() }
            ?.let { CipherEngine.encrypt(masterKey, it.toByteArray(Charsets.UTF_8)) },
        category = category.name,
        createdAt = if (createdAt == 0L) now else createdAt,
        updatedAt = now,
    )
}