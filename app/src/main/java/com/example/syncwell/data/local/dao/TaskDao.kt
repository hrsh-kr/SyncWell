package com.example.syncwell.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.syncwell.data.local.entities.Task
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId AND userId != ''")
    fun getTasksForUser(userId: String): Flow<List<Task>>  // Reactive stream of tasks

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE userId = :userId AND userId != ''")
    suspend fun deleteAllTasksForUser(userId: String)

}