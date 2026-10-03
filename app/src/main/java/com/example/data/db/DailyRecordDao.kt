package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records WHERE weekId = :weekId ORDER BY dayOfWeek ASC")
    fun getRecordsForWeek(weekId: String): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE weekId = :weekId")
    suspend fun getRecordsForWeekDirect(weekId: String): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: String): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAllRecordsDirect(): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): DailyRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<DailyRecordEntity>)

    @Query("UPDATE daily_records SET isCompleted = :isCompleted, numericValue = :numericValue, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRecordCompletion(
        id: String,
        isCompleted: Boolean,
        numericValue: Float?,
        status: DayStatus,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM daily_records WHERE habitId = :habitId")
    suspend fun deleteRecordsForHabit(habitId: String)

    @Query("DELETE FROM daily_records")
    suspend fun clearAll()
}
