package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayNoteEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitEntity
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.data.model.WeekEntity
import com.example.data.model.WeeklyReflection
import com.example.domain.calculator.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HabitRepository(private val db: AppDatabase) {

    private val habitDao = db.habitDao()
    private val weekDao = db.weekDao()
    private val dailyRecordDao = db.dailyRecordDao()
    private val dayNoteDao = db.dayNoteDao()

    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allWeeks: Flow<List<WeekEntity>> = weekDao.getAllWeeks()
    val allRecords: Flow<List<DailyRecordEntity>> = dailyRecordDao.getAllRecords()

    fun getRecordsForWeek(weekId: String): Flow<List<DailyRecordEntity>> =
        dailyRecordDao.getRecordsForWeek(weekId)

    fun getWeek(weekId: String): Flow<WeekEntity?> =
        weekDao.getWeekById(weekId)

    fun getDayNotesForWeek(weekId: String): Flow<List<DayNoteEntity>> =
        dayNoteDao.getDayNotesForWeek(weekId)

    fun getRecordsForHabit(habitId: String): Flow<List<DailyRecordEntity>> =
        dailyRecordDao.getRecordsForHabit(habitId)

    /**
     * Preloads starter habits if database is completely empty.
     */
    suspend fun preloadStarterHabitsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = habitDao.getAllHabits().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val starterHabits = listOf(
                HabitEntity("h_night_routine", "Night Routine", "🌙", "Routine", HabitType.BOOLEAN, 7, 1f, "", true, 1, "Consistent bedtime shutdown"),
                HabitEntity("h_jawline_massage", "Jawline/Head Massage", "💆", "Health", HabitType.BOOLEAN, 7, 1f, "", true, 2, "Facial lymphatic massage"),
                HabitEntity("h_wake_up", "Wake up 9–10", "☀️", "Routine", HabitType.BOOLEAN, 7, 1f, "", true, 3, "Consistent morning rise time"),
                HabitEntity("h_mobile_limit", "Mobile <2hr", "📵", "Discipline", HabitType.LIMIT, 7, 2f, "hrs", true, 4, "Stay under 2 hours screen time"),
                HabitEntity("h_deep_work", "Deep Work >3hr", "💻", "Productivity", HabitType.DURATION, 7, 3f, "hrs", true, 5, "Uninterrupted focused work"),
                HabitEntity("h_exercise", "Exercise", "🏋️", "Health", HabitType.BOOLEAN, 5, 1f, "session", true, 6, "Gym, cardio, or strength workout"),
                HabitEntity("h_ig_limit", "IG <30 min", "📱", "Discipline", HabitType.LIMIT, 7, 30f, "mins", true, 7, "Instagram limit 30 min max"),
                HabitEntity("h_english_talk", "English Talk", "🗣️", "Mindset", HabitType.DURATION, 6, 30f, "mins", true, 8, "English speaking / fluency practice"),
                HabitEntity("h_meditate", "Meditate/Bhajan", "🧘", "Mindset", HabitType.BOOLEAN, 7, 1f, "session", true, 9, "Mindfulness and spiritual connection"),
                HabitEntity("h_yt_upload", "YT Upload", "▶️", "Productivity", HabitType.BOOLEAN, 7, 1f, "", true, 10, "Content creation / upload"),
                HabitEntity("h_nn", "NN", "🚫", "Discipline", HabitType.BOOLEAN, 7, 1f, "", true, 11, "Discipline commitment"),
                HabitEntity("h_journal", "Journal & self-reflect", "📝", "Mindset", HabitType.BOOLEAN, 7, 1f, "", true, 12, "Daily introspection and tracking"),
                HabitEntity("h_bath", "Bath", "🛁", "Health", HabitType.BOOLEAN, 4, 1f, "", true, 13, "Relaxing warm recovery bath")
            )
            habitDao.insertHabits(starterHabits)
        }
    }

    /**
     * Initializes current week, preserving past week data and auto-transitioning if a new week began.
     */
    suspend fun checkAndInitializeCurrentWeek(): String = withContext(Dispatchers.IO) {
        preloadStarterHabitsIfEmpty()

        val todayDate = DateHelper.getTodayDateString()
        val currentMonday = DateHelper.getMondayOfWeek(todayDate)
        val currentSunday = DateHelper.getSundayOfWeek(currentMonday)
        val currentWeekId = DateHelper.getWeekId(currentMonday)

        val existingWeek = weekDao.getWeekDirect(currentWeekId)

        if (existingWeek == null) {
            // Archive previous latest week if exists
            val latestWeek = weekDao.getLatestWeek()
            if (latestWeek != null && latestWeek.weekId != currentWeekId && !latestWeek.isArchived) {
                weekDao.updateWeek(latestWeek.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
            }

            // Snapshot active habits as they exist right now
            val activeHabitsList = habitDao.getActiveHabits().firstOrNull() ?: emptyList()
            val snapshotsJson = JSONArray().apply {
                activeHabitsList.forEach { put(HabitSnapshot.fromEntity(it).toJsonObject()) }
            }.toString()

            val newWeek = WeekEntity(
                weekId = currentWeekId,
                startDate = currentMonday,
                endDate = currentSunday,
                habitSnapshotsJson = snapshotsJson,
                reflectionJson = "{}",
                isArchived = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            weekDao.insertWeek(newWeek)

            // Generate daily records for each day of the week for all active habits
            val records = mutableListOf<DailyRecordEntity>()
            for (habit in activeHabitsList) {
                for (dayNum in 1..7) {
                    val dateForDay = DateHelper.getDateForDayOfWeek(currentMonday, dayNum)
                    val isFuture = DateHelper.isFutureDay(dateForDay, todayDate)
                    val initialStatus = if (isFuture) DayStatus.FUTURE else DayStatus.NOT_COMPLETED

                    val recordId = "${currentWeekId}_${habit.id}_$dayNum"
                    records.add(
                        DailyRecordEntity(
                            id = recordId,
                            weekId = currentWeekId,
                            date = dateForDay,
                            habitId = habit.id,
                            dayOfWeek = dayNum,
                            isCompleted = false,
                            numericValue = null,
                            status = initialStatus
                        )
                    )
                }
            }
            if (records.isNotEmpty()) {
                dailyRecordDao.insertRecords(records)
            }
        } else {
            // Update day statuses if dates have moved forward into the week
            val activeHabitsList = habitDao.getActiveHabits().firstOrNull() ?: emptyList()
            val existingRecords = dailyRecordDao.getRecordsForWeekDirect(currentWeekId)
            val recordsToInsertOrUpdate = mutableListOf<DailyRecordEntity>()

            for (habit in activeHabitsList) {
                for (dayNum in 1..7) {
                    val dateForDay = DateHelper.getDateForDayOfWeek(currentMonday, dayNum)
                    val recordId = "${currentWeekId}_${habit.id}_$dayNum"
                    val existing = existingRecords.find { it.id == recordId }

                    if (existing == null) {
                        val isFuture = DateHelper.isFutureDay(dateForDay, todayDate)
                        recordsToInsertOrUpdate.add(
                            DailyRecordEntity(
                                id = recordId,
                                weekId = currentWeekId,
                                date = dateForDay,
                                habitId = habit.id,
                                dayOfWeek = dayNum,
                                isCompleted = false,
                                numericValue = null,
                                status = if (isFuture) DayStatus.FUTURE else DayStatus.NOT_COMPLETED
                            )
                        )
                    } else if (existing.status == DayStatus.FUTURE && !DateHelper.isFutureDay(dateForDay, todayDate)) {
                        // Day has now arrived! Transition from FUTURE to NOT_COMPLETED
                        recordsToInsertOrUpdate.add(
                            existing.copy(status = DayStatus.NOT_COMPLETED)
                        )
                    }
                }
            }
            if (recordsToInsertOrUpdate.isNotEmpty()) {
                dailyRecordDao.insertRecords(recordsToInsertOrUpdate)
            }
        }

        currentWeekId
    }

    /**
     * Updates completion status of a daily cell.
     */
    suspend fun updateDailyRecord(
        recordId: String,
        isCompleted: Boolean,
        numericValue: Float?,
        status: DayStatus
    ) = withContext(Dispatchers.IO) {
        dailyRecordDao.updateRecordCompletion(recordId, isCompleted, numericValue, status)
    }

    /**
     * Quick-toggles a day cell for boolean habits.
     */
    suspend fun toggleDayCompletion(
        record: DailyRecordEntity,
        habit: HabitSnapshot
    ) = withContext(Dispatchers.IO) {
        val nextCompleted = !record.isCompleted
        val nextStatus = if (nextCompleted) DayStatus.COMPLETED else DayStatus.NOT_COMPLETED
        dailyRecordDao.updateRecordCompletion(
            id = record.id,
            isCompleted = nextCompleted,
            numericValue = if (nextCompleted) habit.dailyTarget else null,
            status = nextStatus
        )
    }

    /**
     * Save daily note (mood, energy, sleep, freeform note).
     */
    suspend fun saveDayNote(note: DayNoteEntity) = withContext(Dispatchers.IO) {
        dayNoteDao.insertDayNote(note)
    }

    /**
     * Save weekly reflection.
     */
    suspend fun saveWeeklyReflection(weekId: String, reflection: WeeklyReflection) = withContext(Dispatchers.IO) {
        weekDao.updateReflection(weekId, reflection.toJsonObject().toString())
    }

    /**
     * Habit management: Add or update habit.
     */
    suspend fun saveHabit(habit: HabitEntity, isNew: Boolean = false) = withContext(Dispatchers.IO) {
        habitDao.insertHabit(habit)
        val todayDate = DateHelper.getTodayDateString()
        val currentMonday = DateHelper.getMondayOfWeek(todayDate)
        val currentWeekId = DateHelper.getWeekId(currentMonday)

        if (isNew) {
            // Generate records for current week if open
            val newRecords = (1..7).map { dayNum ->
                val dateForDay = DateHelper.getDateForDayOfWeek(currentMonday, dayNum)
                val isFuture = DateHelper.isFutureDay(dateForDay, todayDate)
                DailyRecordEntity(
                    id = "${currentWeekId}_${habit.id}_$dayNum",
                    weekId = currentWeekId,
                    date = dateForDay,
                    habitId = habit.id,
                    dayOfWeek = dayNum,
                    isCompleted = false,
                    numericValue = null,
                    status = if (isFuture) DayStatus.FUTURE else DayStatus.NOT_COMPLETED
                )
            }
            dailyRecordDao.insertRecords(newRecords)
        }

        // Keep current week snapshot in sync
        updateCurrentWeekSnapshot(currentWeekId)
    }

    suspend fun setHabitActive(habitId: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        habitDao.setHabitActiveStatus(habitId, isActive)
        val todayDate = DateHelper.getTodayDateString()
        val currentMonday = DateHelper.getMondayOfWeek(todayDate)
        val currentWeekId = DateHelper.getWeekId(currentMonday)
        updateCurrentWeekSnapshot(currentWeekId)
    }

    suspend fun updateHabitOrder(habitId: String, order: Int) = withContext(Dispatchers.IO) {
        habitDao.updateDisplayOrder(habitId, order)
        val todayDate = DateHelper.getTodayDateString()
        val currentMonday = DateHelper.getMondayOfWeek(todayDate)
        val currentWeekId = DateHelper.getWeekId(currentMonday)
        updateCurrentWeekSnapshot(currentWeekId)
    }

    suspend fun deleteHabit(habitId: String) = withContext(Dispatchers.IO) {
        habitDao.deleteHabit(habitId)
        dailyRecordDao.deleteRecordsForHabit(habitId)
        val todayDate = DateHelper.getTodayDateString()
        val currentMonday = DateHelper.getMondayOfWeek(todayDate)
        val currentWeekId = DateHelper.getWeekId(currentMonday)
        updateCurrentWeekSnapshot(currentWeekId)
    }

    private suspend fun updateCurrentWeekSnapshot(currentWeekId: String) {
        val currentWeek = weekDao.getWeekDirect(currentWeekId)
        if (currentWeek != null) {
            val activeList: List<HabitEntity> = habitDao.getActiveHabitsList()
            val jsonArr = org.json.JSONArray()
            activeList.forEach { h ->
                jsonArr.put(HabitSnapshot.fromEntity(h).toJsonObject())
            }
            weekDao.updateHabitSnapshots(currentWeekId, jsonArr.toString())
        }
    }

    /**
     * Export all data to JSON string for backup.
     */
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "LocalHabitTracker")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Habits
        val habits = habitDao.getAllHabits().firstOrNull() ?: emptyList()
        val habitsArr = JSONArray()
        for (h in habits) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("icon", h.icon)
                put("category", h.category)
                put("trackingType", h.trackingType.name)
                put("weeklyTarget", h.weeklyTarget)
                put("dailyTarget", h.dailyTarget.toDouble())
                put("unit", h.unit)
                put("isActive", h.isActive)
                put("displayOrder", h.displayOrder)
                put("notes", h.notes)
                put("createdAt", h.createdAt)
            }
            habitsArr.put(obj)
        }
        root.put("habits", habitsArr)

        // Weeks
        val weeks = weekDao.getAllWeeks().firstOrNull() ?: emptyList()
        val weeksArr = JSONArray()
        for (w in weeks) {
            val obj = JSONObject().apply {
                put("weekId", w.weekId)
                put("startDate", w.startDate)
                put("endDate", w.endDate)
                put("habitSnapshotsJson", w.habitSnapshotsJson)
                put("reflectionJson", w.reflectionJson)
                put("isArchived", w.isArchived)
                put("createdAt", w.createdAt)
                put("updatedAt", w.updatedAt)
            }
            weeksArr.put(obj)
        }
        root.put("weeks", weeksArr)

        // Daily records
        val records = dailyRecordDao.getAllRecordsDirect()
        val recArr = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("weekId", r.weekId)
                put("date", r.date)
                put("habitId", r.habitId)
                put("dayOfWeek", r.dayOfWeek)
                put("isCompleted", r.isCompleted)
                if (r.numericValue != null) put("numericValue", r.numericValue.toDouble())
                put("status", r.status.name)
                put("updatedAt", r.updatedAt)
            }
            recArr.put(obj)
        }
        root.put("dailyRecords", recArr)

        // Day notes
        val notes = dayNoteDao.getAllDayNotesDirect()
        val notesArr = JSONArray()
        for (n in notes) {
            val obj = JSONObject().apply {
                put("date", n.date)
                put("weekId", n.weekId)
                if (n.mood != null) put("mood", n.mood)
                if (n.energy != null) put("energy", n.energy)
                if (n.sleepHours != null) put("sleepHours", n.sleepHours.toDouble())
                if (n.note != null) put("note", n.note)
            }
            notesArr.put(obj)
        }
        root.put("dayNotes", notesArr)

        root.toString(2)
    }

    /**
     * Import and restore data from JSON backup.
     */
    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("habits") || !root.has("weeks")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid habit backup JSON format"))
            }

            val habitsArr = root.getJSONArray("habits")
            val importedHabits = mutableListOf<HabitEntity>()
            for (i in 0 until habitsArr.length()) {
                val obj = habitsArr.getJSONObject(i)
                importedHabits.add(
                    HabitEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        icon = obj.optString("icon", "⭐"),
                        category = obj.optString("category", "General"),
                        trackingType = try { HabitType.valueOf(obj.getString("trackingType")) } catch (e: Exception) { HabitType.BOOLEAN },
                        weeklyTarget = obj.getInt("weeklyTarget"),
                        dailyTarget = obj.optDouble("dailyTarget", 1.0).toFloat(),
                        unit = obj.optString("unit", ""),
                        isActive = obj.optBoolean("isActive", true),
                        displayOrder = obj.optInt("displayOrder", 0),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val weeksArr = root.getJSONArray("weeks")
            val importedWeeks = mutableListOf<WeekEntity>()
            for (i in 0 until weeksArr.length()) {
                val obj = weeksArr.getJSONObject(i)
                importedWeeks.add(
                    WeekEntity(
                        weekId = obj.getString("weekId"),
                        startDate = obj.getString("startDate"),
                        endDate = obj.getString("endDate"),
                        habitSnapshotsJson = obj.optString("habitSnapshotsJson", "[]"),
                        reflectionJson = obj.optString("reflectionJson", "{}"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val recArr = root.optJSONArray("dailyRecords")
            val importedRecords = mutableListOf<DailyRecordEntity>()
            if (recArr != null) {
                for (i in 0 until recArr.length()) {
                    val obj = recArr.getJSONObject(i)
                    importedRecords.add(
                        DailyRecordEntity(
                            id = obj.getString("id"),
                            weekId = obj.getString("weekId"),
                            date = obj.getString("date"),
                            habitId = obj.getString("habitId"),
                            dayOfWeek = obj.getInt("dayOfWeek"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            numericValue = if (obj.has("numericValue")) obj.getDouble("numericValue").toFloat() else null,
                            status = try { DayStatus.valueOf(obj.getString("status")) } catch (e: Exception) { DayStatus.FUTURE },
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val notesArr = root.optJSONArray("dayNotes")
            val importedNotes = mutableListOf<DayNoteEntity>()
            if (notesArr != null) {
                for (i in 0 until notesArr.length()) {
                    val obj = notesArr.getJSONObject(i)
                    importedNotes.add(
                        DayNoteEntity(
                            date = obj.getString("date"),
                            weekId = obj.getString("weekId"),
                            mood = if (obj.has("mood")) obj.getInt("mood") else null,
                            energy = if (obj.has("energy")) obj.getInt("energy") else null,
                            sleepHours = if (obj.has("sleepHours")) obj.getDouble("sleepHours").toFloat() else null,
                            note = if (obj.has("note")) obj.getString("note") else null
                        )
                    )
                }
            }

            // Restore tables
            habitDao.clearAll()
            weekDao.clearAll()
            dailyRecordDao.clearAll()
            dayNoteDao.clearAll()

            habitDao.insertHabits(importedHabits)
            weekDao.insertWeeks(importedWeeks)
            if (importedRecords.isNotEmpty()) dailyRecordDao.insertRecords(importedRecords)
            if (importedNotes.isNotEmpty()) dayNoteDao.insertDayNotes(importedNotes)

            Result.success(importedHabits.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Wipes all data and re-initializes starter habits and fresh week.
     */
    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        habitDao.clearAll()
        weekDao.clearAll()
        dailyRecordDao.clearAll()
        dayNoteDao.clearAll()

        preloadStarterHabitsIfEmpty()
        checkAndInitializeCurrentWeek()
    }
}
