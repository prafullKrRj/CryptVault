package com.cryptvault.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_entries ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<VaultEntryEntity>>

    @Query("SELECT * FROM vault_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VaultEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: VaultEntryEntity): Long

    @Update
    suspend fun update(entry: VaultEntryEntity)

    @Delete
    suspend fun delete(entry: VaultEntryEntity)

    @Query("DELETE FROM vault_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM vault_entries")
    suspend fun count(): Int
}