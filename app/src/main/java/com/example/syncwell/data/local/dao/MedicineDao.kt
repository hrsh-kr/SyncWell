package com.example.syncwell.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.syncwell.data.local.entities.Medicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE userId = :userId AND userId != ''")
    fun getMedicinesForUser(userId: String): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE userId = :userId AND userId != ''")
    suspend fun deleteAllMedicinesForUser(userId: String)

    @Query("SELECT * FROM medicines WHERE userId = :userId AND userId != '' AND timeMillis <= :hourFromNow")
    fun getMedicinesDueBefore(userId: String, hourFromNow: Long): Flow<List<Medicine>>
}