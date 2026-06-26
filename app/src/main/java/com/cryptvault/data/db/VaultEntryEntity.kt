package com.cryptvault.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cryptvault.domain.model.EntryCategory

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val username: String,
    val passwordBlob: ByteArray,
    val notesBlob: ByteArray?,
    val category: String = EntryCategory.Login.name,
    val createdAt: Long,
    val updatedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultEntryEntity) return false
        return id == other.id &&
            title == other.title &&
            username == other.username &&
            passwordBlob.contentEquals(other.passwordBlob) &&
            (notesBlob?.contentEquals(other.notesBlob) ?: (other.notesBlob == null)) &&
            category == other.category &&
            createdAt == other.createdAt &&
            updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + passwordBlob.contentHashCode()
        result = 31 * result + (notesBlob?.contentHashCode() ?: 0)
        result = 31 * result + category.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}