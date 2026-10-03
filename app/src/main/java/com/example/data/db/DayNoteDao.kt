package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DayNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayNoteDao {
    @Query("SELECT * FROM day_notes WHERE weekId = :weekId")
    fun getDayNotesForWeek(weekId: String): Flow<List<DayNoteEntity>>

    @Query("SELECT * FROM day_notes WHERE date = :date LIMIT 1")
    suspend fun getDayNoteByDate(date: String): DayNoteEntity?

    @Query("SELECT * FROM day_notes")
    fun getAllDayNotes(): Flow<List<DayNoteEntity>>

    @Query("SELECT * FROM day_notes")
    suspend fun getAllDayNotesDirect(): List<DayNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayNote(note: DayNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayNotes(notes: List<DayNoteEntity>)

    @Query("DELETE FROM day_notes")
    suspend fun clearAll()
}
