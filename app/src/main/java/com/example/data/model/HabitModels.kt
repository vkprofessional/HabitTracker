package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class HabitType {
    BOOLEAN,
    NUMERIC,
    DURATION,
    LIMIT
}

enum class DayStatus {
    COMPLETED,
    NOT_COMPLETED,
    NOT_APPLICABLE,
    FUTURE
}

@Entity(
    tableName = "habits",
    indices = [Index(value = ["isActive", "displayOrder"])]
)
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val trackingType: HabitType,
    val weeklyTarget: Int,
    val dailyTarget: Float = 1f,
    val unit: String = "",
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "weeks",
    indices = [Index(value = ["startDate"], unique = true)]
)
data class WeekEntity(
    @PrimaryKey val weekId: String, // e.g. "2026-W39"
    val startDate: String,          // e.g. "2026-09-21"
    val endDate: String,            // e.g. "2026-09-27"
    val habitSnapshotsJson: String, // JSON Array of HabitSnapshot
    val reflectionJson: String,     // JSON of WeeklyReflection
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_records",
    indices = [
        Index(value = ["weekId"]),
        Index(value = ["habitId"]),
        Index(value = ["date"])
    ]
)
data class DailyRecordEntity(
    @PrimaryKey val id: String, // "${weekId}_${habitId}_${dayOfWeek}"
    val weekId: String,
    val date: String,           // "2026-09-21"
    val habitId: String,
    val dayOfWeek: Int,         // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val isCompleted: Boolean = false,
    val numericValue: Float? = null,
    val status: DayStatus = DayStatus.FUTURE,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "day_notes"
)
data class DayNoteEntity(
    @PrimaryKey val date: String, // "2026-09-21"
    val weekId: String,
    val mood: Int? = null,       // 1 to 10
    val energy: Int? = null,     // 1 to 10
    val sleepHours: Float? = null,
    val note: String? = null
)

// Snapshot of a habit preserved inside a weekly record
data class HabitSnapshot(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val trackingType: HabitType,
    val weeklyTarget: Int,
    val dailyTarget: Float = 1f,
    val unit: String = "",
    val displayOrder: Int = 0,
    val notes: String = ""
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("icon", icon)
            put("category", category)
            put("trackingType", trackingType.name)
            put("weeklyTarget", weeklyTarget)
            put("dailyTarget", dailyTarget.toDouble())
            put("unit", unit)
            put("displayOrder", displayOrder)
            put("notes", notes)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): HabitSnapshot {
            return HabitSnapshot(
                id = obj.optString("id", ""),
                name = obj.optString("name", "Unnamed Habit"),
                icon = obj.optString("icon", "⭐"),
                category = obj.optString("category", "General"),
                trackingType = try {
                    HabitType.valueOf(obj.optString("trackingType", HabitType.BOOLEAN.name))
                } catch (e: Exception) {
                    HabitType.BOOLEAN
                },
                weeklyTarget = obj.optInt("weeklyTarget", 7),
                dailyTarget = obj.optDouble("dailyTarget", 1.0).toFloat(),
                unit = obj.optString("unit", ""),
                displayOrder = obj.optInt("displayOrder", 0),
                notes = obj.optString("notes", "")
            )
        }

        fun fromEntity(entity: HabitEntity): HabitSnapshot {
            return HabitSnapshot(
                id = entity.id,
                name = entity.name,
                icon = entity.icon,
                category = entity.category,
                trackingType = entity.trackingType,
                weeklyTarget = entity.weeklyTarget,
                dailyTarget = entity.dailyTarget,
                unit = entity.unit,
                displayOrder = entity.displayOrder,
                notes = entity.notes
            )
        }
    }
}

data class WeeklyReflection(
    val whatWentWell: String = "",
    val whatWentBadly: String = "",
    val whyMissedHabits: String = "",
    val whatToChangeNextWeek: String = "",
    val mainPriority: String = ""
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("whatWentWell", whatWentWell)
            put("whatWentBadly", whatWentBadly)
            put("whyMissedHabits", whyMissedHabits)
            put("whatToChangeNextWeek", whatToChangeNextWeek)
            put("mainPriority", mainPriority)
        }
    }

    companion object {
        fun fromJson(jsonStr: String?): WeeklyReflection {
            if (jsonStr.isNullOrBlank()) return WeeklyReflection()
            return try {
                val obj = JSONObject(jsonStr)
                WeeklyReflection(
                    whatWentWell = obj.optString("whatWentWell", ""),
                    whatWentBadly = obj.optString("whatWentBadly", ""),
                    whyMissedHabits = obj.optString("whyMissedHabits", ""),
                    whatToChangeNextWeek = obj.optString("whatToChangeNextWeek", ""),
                    mainPriority = obj.optString("mainPriority", "")
                )
            } catch (e: Exception) {
                WeeklyReflection()
            }
        }
    }
}

data class NextWeekSuggestion(
    val habitId: String,
    val habitName: String,
    val icon: String,
    val previousTarget: Int,
    val previousCompletion: Int,
    val trend: String, // "Improved", "Declined", "Stable"
    val suggestedAction: String, // "Keep target", "Increase target", "Reduce target", "Change tracking method", "Review habit"
    val rationalReason: String
)

data class HabitWeekProgress(
    val habit: HabitSnapshot,
    val targetDays: Int,
    val completedDays: Int,
    val successRate: Float, // 0f to 1f
    val isAdherent: Boolean,
    val dayRecords: List<DailyRecordEntity>
)

data class WeekComparison(
    val currentScore: Int,
    val previousScore: Int,
    val scoreDelta: Int,
    val habitsImproved: List<String>,
    val habitsDeclined: List<String>,
    val habitsStable: List<String>
)

data class WeeklyInsightSummary(
    val weekId: String,
    val dateRange: String,
    val overallPercentage: Int,
    val targetAdherenceCount: Int,
    val totalHabitsCount: Int,
    val bestHabit: String?,
    val weakestHabit: String?,
    val bestDay: String?,
    val weakestDay: String?,
    val totalSuccessfulHabitDays: Int,
    val missedHabitDays: Int,
    val comparison: WeekComparison?,
    val habitProgressList: List<HabitWeekProgress>
)

data class PatternInsight(
    val title: String,
    val description: String,
    val type: String, // "DAY_PERFORMANCE", "HABIT_CORRELATION", "STREAK", "CONSISTENCY"
    val isPositive: Boolean
)
