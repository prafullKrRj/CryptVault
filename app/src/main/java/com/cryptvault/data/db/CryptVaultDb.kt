package com.cryptvault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VaultEntryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class CryptVaultDb : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        const val DB_NAME = "cryptvault.db"
    }
}