package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY displayOrder ASC, createdAt ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY displayOrder ASC, createdAt ASC")
    suspend fun getActiveHabitsList(): List<HabitEntity>

    @Query("SELECT * FROM habits ORDER BY isActive DESC, displayOrder ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = :isActive WHERE id = :id")
    suspend fun setHabitActiveStatus(id: String, isActive: Boolean)

    @Query("UPDATE habits SET displayOrder = :order WHERE id = :id")
    suspend fun updateDisplayOrder(id: String, order: Int)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)

    @Query("DELETE FROM habits")
    suspend fun clearAll()
}
