package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WeekEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekDao {
    @Query("SELECT * FROM weeks ORDER BY startDate DESC")
    fun getAllWeeks(): Flow<List<WeekEntity>>

    @Query("SELECT * FROM weeks WHERE weekId = :weekId LIMIT 1")
    fun getWeekById(weekId: String): Flow<WeekEntity?>

    @Query("SELECT * FROM weeks WHERE weekId = :weekId LIMIT 1")
    suspend fun getWeekDirect(weekId: String): WeekEntity?

    @Query("SELECT * FROM weeks ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestWeek(): WeekEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeek(week: WeekEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeks(weeks: List<WeekEntity>)

    @Update
    suspend fun updateWeek(week: WeekEntity)

    @Query("UPDATE weeks SET reflectionJson = :reflectionJson, updatedAt = :updatedAt WHERE weekId = :weekId")
    suspend fun updateReflection(weekId: String, reflectionJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE weeks SET habitSnapshotsJson = :snapshotsJson, updatedAt = :updatedAt WHERE weekId = :weekId")
    suspend fun updateHabitSnapshots(weekId: String, snapshotsJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM weeks")
    suspend fun clearAll()
}
