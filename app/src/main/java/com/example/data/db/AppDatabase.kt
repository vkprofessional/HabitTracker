package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayNoteEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitEntity
import com.example.data.model.HabitType
import com.example.data.model.WeekEntity

class Converters {
    @TypeConverter
    fun fromHabitType(type: HabitType?): String = type?.name ?: HabitType.BOOLEAN.name

    @TypeConverter
    fun toHabitType(value: String?): HabitType {
        return try {
            if (value != null) HabitType.valueOf(value) else HabitType.BOOLEAN
        } catch (e: Exception) {
            HabitType.BOOLEAN
        }
    }

    @TypeConverter
    fun fromDayStatus(status: DayStatus?): String = status?.name ?: DayStatus.FUTURE.name

    @TypeConverter
    fun toDayStatus(value: String?): DayStatus {
        return try {
            if (value != null) DayStatus.valueOf(value) else DayStatus.FUTURE
        } catch (e: Exception) {
            DayStatus.FUTURE
        }
    }
}

@Database(
    entities = [
        HabitEntity::class,
        WeekEntity::class,
        DailyRecordEntity::class,
        DayNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun weekDao(): WeekDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun dayNoteDao(): DayNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
