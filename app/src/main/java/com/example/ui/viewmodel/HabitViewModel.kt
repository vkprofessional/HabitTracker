package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayNoteEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitEntity
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.data.model.HabitWeekProgress
import com.example.data.model.NextWeekSuggestion
import com.example.data.model.PatternInsight
import com.example.data.model.WeekEntity
import com.example.data.model.WeeklyInsightSummary
import com.example.data.model.WeeklyReflection
import com.example.data.repository.HabitRepository
import com.example.domain.calculator.DateHelper
import com.example.domain.calculator.HabitCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.UUID

enum class ScreenDestination {
    THIS_WEEK,
    WEEKLY_REVIEW,
    HISTORY,
    ANALYTICS,
    HABITS,
    SETTINGS
}

data class HabitUiState(
    val currentWeekId: String = "",
    val inspectedWeekId: String = "",
    val todayDate: String = DateHelper.getTodayDateString(),
    val activeHabits: List<HabitEntity> = emptyList(),
    val allHabits: List<HabitEntity> = emptyList(),
    val allWeeks: List<WeekEntity> = emptyList(),
    val currentWeekEntity: WeekEntity? = null,
    val currentWeekRecords: List<DailyRecordEntity> = emptyList(),
    val currentWeekNotes: List<DayNoteEntity> = emptyList(),
    val allRecords: List<DailyRecordEntity> = emptyList(),
    val currentWeekProgress: List<HabitWeekProgress> = emptyList(),
    val currentWeekSummary: WeeklyInsightSummary? = null,
    val previousWeekSummary: WeeklyInsightSummary? = null,
    val nextWeekSuggestions: List<NextWeekSuggestion> = emptyList(),
    val patternInsights: List<PatternInsight> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(db)

    private val _currentScreen = MutableStateFlow(ScreenDestination.THIS_WEEK)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Screen selection and dialog states
    private val _selectedHabitDetail = MutableStateFlow<HabitSnapshot?>(null)
    val selectedHabitDetail: StateFlow<HabitSnapshot?> = _selectedHabitDetail.asStateFlow()

    private val _selectedHistoricalWeek = MutableStateFlow<WeekEntity?>(null)
    val selectedHistoricalWeek: StateFlow<WeekEntity?> = _selectedHistoricalWeek.asStateFlow()

    private val _editingCellRecord = MutableStateFlow<Pair<DailyRecordEntity, HabitSnapshot>?>(null)
    val editingCellRecord: StateFlow<Pair<DailyRecordEntity, HabitSnapshot>?> = _editingCellRecord.asStateFlow()

    private val _editingDayNote = MutableStateFlow<DayNoteEntity?>(null)
    val editingDayNote: StateFlow<DayNoteEntity?> = _editingDayNote.asStateFlow()

    private val _editingHabit = MutableStateFlow<HabitEntity?>(null)
    val editingHabit: StateFlow<HabitEntity?> = _editingHabit.asStateFlow()
    val isCreatingNewHabit = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    fun initializeData() {
        viewModelScope.launch {
            val weekId = repository.checkAndInitializeCurrentWeek()
            _uiState.value = _uiState.value.copy(
                currentWeekId = weekId,
                inspectedWeekId = weekId
            )

            // Start observing changes
            observeData()
        }
    }

    private var analyticsJob: Job? = null

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.activeHabits,
                repository.allHabits,
                repository.allWeeks,
                repository.allRecords
            ) { active, all, weeks, records ->
                withContext(Dispatchers.Default) {
                    val todayDate = DateHelper.getTodayDateString()
                    val currentMonday = DateHelper.getMondayOfWeek(todayDate)
                    val currentWeekId = DateHelper.getWeekId(currentMonday)

                    val currentWeek = weeks.find { it.weekId == currentWeekId }
                    val currentRecords = records.filter { it.weekId == currentWeekId }

                    // ALWAYS use live active habits for the current week so any edit immediately updates the tracker
                    val habitSnapshots = active.map { HabitSnapshot.fromEntity(it) }

                    // Compute habit progress for current week
                    val currentProgress = habitSnapshots.map { habit ->
                        val habitRecords = currentRecords.filter { it.habitId == habit.id }
                        HabitCalculator.calculateHabitProgress(habit, habitRecords, todayDate)
                    }

                    // Previous week data
                    val prevMonday = DateHelper.getPreviousMonday(currentMonday)
                    val prevWeekId = DateHelper.getWeekId(prevMonday)
                    val prevWeek = weeks.find { it.weekId == prevWeekId }
                    val prevRecords = records.filter { it.weekId == prevWeekId }

                    val prevProgress = if (prevWeek != null && prevWeek.habitSnapshotsJson.isNotBlank()) {
                        parseHabitSnapshots(prevWeek.habitSnapshotsJson).map { habit ->
                            val habitRecs = prevRecords.filter { it.habitId == habit.id }
                            HabitCalculator.calculateHabitProgress(habit, habitRecs, todayDate)
                        }
                    } else null

                    // Summaries
                    val currentSummary = HabitCalculator.generateWeeklyInsights(
                        weekId = currentWeekId,
                        dateRange = DateHelper.getWeekDisplayRange(currentMonday),
                        habitProgressList = currentProgress,
                        allWeekRecords = currentRecords,
                        previousWeekProgress = prevProgress,
                        todayDate = todayDate
                    )

                    val prevSummary = if (prevWeek != null && prevProgress != null) {
                        HabitCalculator.generateWeeklyInsights(
                            weekId = prevWeekId,
                            dateRange = DateHelper.getWeekDisplayRange(prevMonday),
                            habitProgressList = prevProgress,
                            allWeekRecords = prevRecords,
                            previousWeekProgress = null,
                            todayDate = todayDate
                        )
                    } else null

                    // Immediately update UI state
                    _uiState.update { prev ->
                        prev.copy(
                            currentWeekId = currentWeekId,
                            inspectedWeekId = currentWeekId,
                            todayDate = todayDate,
                            activeHabits = active,
                            allHabits = all,
                            allWeeks = weeks,
                            currentWeekEntity = currentWeek,
                            currentWeekRecords = currentRecords,
                            allRecords = records,
                            currentWeekProgress = currentProgress,
                            currentWeekSummary = currentSummary,
                            previousWeekSummary = prevSummary,
                            isLoading = false
                        )
                    }

                    // Asynchronously compute heavy suggestions & pattern analytics in background without blocking day clicks
                    scheduleBackgroundAnalytics(currentProgress, prevProgress, records, habitSnapshots)
                }
            }.collect {}
        }
    }

    private fun scheduleBackgroundAnalytics(
        currentProgress: List<HabitWeekProgress>,
        prevProgress: List<HabitWeekProgress>?,
        records: List<DailyRecordEntity>,
        habitSnapshots: List<HabitSnapshot>
    ) {
        analyticsJob?.cancel()
        analyticsJob = viewModelScope.launch(Dispatchers.Default) {
            val nextWeekSuggestions = HabitCalculator.generateNextWeekRecommendations(
                currentHabits = currentProgress,
                previousHabits = prevProgress
            )
            val patternInsights = HabitCalculator.detectPersonalPatterns(
                allRecords = records,
                habits = habitSnapshots
            )
            _uiState.update { prev ->
                prev.copy(
                    nextWeekSuggestions = nextWeekSuggestions,
                    patternInsights = patternInsights
                )
            }
        }
    }

    private fun parseHabitSnapshots(jsonString: String): List<HabitSnapshot> {
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<HabitSnapshot>()
            for (i in 0 until arr.length()) {
                list.add(HabitSnapshot.fromJsonObject(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun openHabitDetail(habit: HabitSnapshot) {
        _selectedHabitDetail.value = habit
    }

    fun closeHabitDetail() {
        _selectedHabitDetail.value = null
    }

    fun openHistoricalWeek(week: WeekEntity) {
        _selectedHistoricalWeek.value = week
    }

    fun closeHistoricalWeek() {
        _selectedHistoricalWeek.value = null
    }

    fun openCellEditor(record: DailyRecordEntity, habit: HabitSnapshot) {
        _editingCellRecord.value = Pair(record, habit)
    }

    fun closeCellEditor() {
        _editingCellRecord.value = null
    }

    fun openDayNoteEditor(date: String) {
        viewModelScope.launch {
            val existing = db.dayNoteDao().getDayNoteByDate(date)
            _editingDayNote.value = existing ?: DayNoteEntity(
                date = date,
                weekId = _uiState.value.currentWeekId
            )
        }
    }

    fun closeDayNoteEditor() {
        _editingDayNote.value = null
    }

    fun openHabitEditor(habit: HabitEntity?, isNew: Boolean = false) {
        _editingHabit.value = habit ?: HabitEntity(
            id = "habit_${UUID.randomUUID().toString().take(8)}",
            name = "",
            icon = "🎯",
            category = "General",
            trackingType = HabitType.BOOLEAN,
            weeklyTarget = 7,
            dailyTarget = 1f,
            unit = "",
            isActive = true,
            displayOrder = (_uiState.value.allHabits.maxOfOrNull { it.displayOrder } ?: 0) + 1,
            notes = ""
        )
        isCreatingNewHabit.value = isNew
    }

    fun closeHabitEditor() {
        _editingHabit.value = null
        isCreatingNewHabit.value = false
    }

    fun toggleCell(record: DailyRecordEntity, habit: HabitSnapshot) {
        viewModelScope.launch {
            if (habit.trackingType == HabitType.BOOLEAN) {
                repository.toggleDayCompletion(record, habit)
            } else {
                // For numeric / duration / limit, open cell editor dialog for precision
                openCellEditor(record, habit)
            }
        }
    }

    fun updateCellRecord(
        recordId: String,
        isCompleted: Boolean,
        numericValue: Float?,
        status: DayStatus
    ) {
        viewModelScope.launch {
            repository.updateDailyRecord(recordId, isCompleted, numericValue, status)
            closeCellEditor()
        }
    }

    fun saveDayNote(note: DayNoteEntity) {
        viewModelScope.launch {
            repository.saveDayNote(note)
            closeDayNoteEditor()
            showMessage("Daily note saved")
        }
    }

    fun saveWeeklyReflection(reflection: WeeklyReflection) {
        viewModelScope.launch {
            val weekId = _uiState.value.currentWeekId
            repository.saveWeeklyReflection(weekId, reflection)
            showMessage("Weekly reflection saved permanently")
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.saveHabit(habit, isCreatingNewHabit.value)
            closeHabitEditor()
            showMessage(if (isCreatingNewHabit.value) "Habit created" else "Habit updated")
        }
    }

    fun toggleHabitActive(habitId: String, currentActive: Boolean) {
        viewModelScope.launch {
            repository.setHabitActive(habitId, !currentActive)
            showMessage(if (!currentActive) "Habit reactivated" else "Habit archived (preserved in history)")
        }
    }

    fun moveHabitOrder(habitId: String, direction: Int) {
        viewModelScope.launch {
            val habits = _uiState.value.allHabits
            val index = habits.indexOfFirst { it.id == habitId }
            val targetIndex = index + direction
            if (index != -1 && targetIndex in habits.indices) {
                val currentHabit = habits[index]
                val targetHabit = habits[targetIndex]
                repository.updateHabitOrder(currentHabit.id, targetHabit.displayOrder)
                repository.updateHabitOrder(targetHabit.id, currentHabit.displayOrder)
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            showMessage("Habit deleted")
        }
    }

    fun exportBackupJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportToJson()
            onExportReady(json)
            showMessage("Export backup generated successfully")
        }
    }

    fun importBackupJson(jsonString: String) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString)
            if (result.isSuccess) {
                showMessage("Backup restored! Loaded ${result.getOrNull()} habits.")
                initializeData()
            } else {
                showMessage("Import failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            showMessage("All data reset to fresh starter habits.")
            initializeData()
        }
    }

    fun showMessage(msg: String) {
        _uiState.value = _uiState.value.copy(userMessage = msg)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
}
