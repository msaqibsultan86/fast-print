package com.notzyvex.fastprint.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PrintJobEntity>>

    @Query("SELECT * FROM print_jobs WHERE id = :id")
    suspend fun byId(id: Long): PrintJobEntity?

    @Insert
    suspend fun insert(job: PrintJobEntity): Long

    @Query("DELETE FROM print_jobs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM print_jobs")
    suspend fun all(): List<PrintJobEntity>

    @Query("DELETE FROM print_jobs")
    suspend fun clear()
}
