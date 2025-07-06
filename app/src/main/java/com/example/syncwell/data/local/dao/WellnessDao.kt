package com.example.syncwell.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.syncwell.data.local.entities.WellnessEntry
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete
import org.threeten.bp.LocalDate

@Dao
interface WellnessDao {
    @Query("SELECT * FROM wellness_entries WHERE userId = :userId AND userId != '' ORDER BY timestamp DESC")
    fun getEntriesForUser(userId: String): Flow<List<WellnessEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WellnessEntry)

    @Delete()
    suspend fun deleteEntry(entry: WellnessEntry)

    @Query("DELETE FROM wellness_entries WHERE userId = :userId AND userId != ''")
    suspend fun deleteAllEntriesForUser(userId: String)

    @Query("SELECT * FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEntriesForDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<WellnessEntry>>

    @Query("SELECT * FROM wellness_entries WHERE userId = :userId AND userId != '' AND moodRating = :rating ORDER BY timestamp DESC")
    fun getEntriesByMoodRating(userId: String, rating: Int): Flow<List<WellnessEntry>>

    @Query("SELECT AVG(moodRating) FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageMoodForPeriod(userId: String, startTime: Long, endTime: Long): Float?

    @Query("SELECT AVG(sleepHours) FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageSleepForPeriod(userId: String, startTime: Long, endTime: Long): Float?

    @Query("SELECT AVG(energyLevel) FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageEnergyForPeriod(userId: String, startTime: Long, endTime: Long): Float?

    @Query("SELECT COUNT(*) FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getEntryCountForPeriod(userId: String, startTime: Long, endTime: Long): Int
    
    /**
     * Count entries for a specific day by using the timestamp to identify entries from that day
     * 
     * @param userId The user ID to filter by
     * @param dayStartTime Timestamp for the start of the day
     * @param dayEndTime Timestamp for the end of the day
     * @return Count of entries for that day
     */
    @Query("SELECT COUNT(*) FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :dayStartTime AND :dayEndTime")
    suspend fun getEntryCountForDay(userId: String, dayStartTime: Long, dayEndTime: Long): Int
    
    /**
     * Get an entry for a specific day
     * 
     * @param userId The user ID to filter by
     * @param dayStartTime Timestamp for the start of the day
     * @param dayEndTime Timestamp for the end of the day
     * @return Entry for that day, or null if none exists
     */
    @Query("SELECT * FROM wellness_entries WHERE userId = :userId AND userId != '' AND timestamp BETWEEN :dayStartTime AND :dayEndTime LIMIT 1")
    suspend fun getEntryForDay(userId: String, dayStartTime: Long, dayEndTime: Long): WellnessEntry?
}