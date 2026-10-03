package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CellEditDialog
import com.example.ui.components.DayNoteDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HabitEditDialog
import com.example.ui.screens.HabitHistoryScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThisWeekScreen
import com.example.ui.screens.WeeklyReviewScreen
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HabitViewModel
import com.example.ui.viewmodel.ScreenDestination

data class NavItem(
    val destination: ScreenDestination,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HabitTrackerApp()
            }
        }
    }
}

@Composable
fun HabitTrackerApp(viewModel: HabitViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedHabitDetail by viewModel.selectedHabitDetail.collectAsStateWithLifecycle()
    val selectedHistoricalWeek by viewModel.selectedHistoricalWeek.collectAsStateWithLifecycle()
    val editingCell by viewModel.editingCellRecord.collectAsStateWithLifecycle()
    val editingDayNote by viewModel.editingDayNote.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()
    val isCreatingNewHabit by viewModel.isCreatingNewHabit.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Handle back button for sub-screens
    BackHandler(enabled = selectedHabitDetail != null || selectedHistoricalWeek != null || currentScreen != ScreenDestination.THIS_WEEK) {
        when {
            selectedHabitDetail != null -> viewModel.closeHabitDetail()
            selectedHistoricalWeek != null -> viewModel.closeHistoricalWeek()
            currentScreen == ScreenDestination.HISTORY -> viewModel.navigateTo(ScreenDestination.SETTINGS)
            currentScreen != ScreenDestination.THIS_WEEK -> viewModel.navigateTo(ScreenDestination.THIS_WEEK)
        }
    }

    val navItems = listOf(
        NavItem(ScreenDestination.THIS_WEEK, "This Week", Icons.Default.Today, "nav_this_week"),
        NavItem(ScreenDestination.WEEKLY_REVIEW, "Review", Icons.Default.Insights, "nav_review"),
        NavItem(ScreenDestination.ANALYTICS, "Analytics", Icons.Default.BarChart, "nav_analytics"),
        NavItem(ScreenDestination.HABITS, "Habits", Icons.Default.FormatListBulleted, "nav_habits"),
        NavItem(ScreenDestination.SETTINGS, "Settings", Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedHabitDetail == null && selectedHistoricalWeek == null) {
                NavigationBar(
                    containerColor = CardSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.destination) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldSuccess,
                                selectedTextColor = EmeraldSuccess,
                                indicatorColor = CardSurface,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(innerPadding)
        ) {
            when {
                selectedHabitDetail != null -> {
                    HabitHistoryScreen(
                        habit = selectedHabitDetail!!,
                        state = uiState,
                        onBack = { viewModel.closeHabitDetail() }
                    )
                }
                selectedHistoricalWeek != null -> {
                    HistoryScreen(
                        state = uiState,
                        selectedWeek = selectedHistoricalWeek,
                        onSelectWeek = { viewModel.openHistoricalWeek(it) },
                        onCloseDetail = { viewModel.closeHistoricalWeek() }
                    )
                }
                else -> {
                    when (currentScreen) {
                        ScreenDestination.THIS_WEEK -> {
                            ThisWeekScreen(
                                state = uiState,
                                onCellClick = { rec, habit -> viewModel.toggleCell(rec, habit) },
                                onCellLongClick = { rec, habit -> viewModel.openCellEditor(rec, habit) },
                                onHabitClick = { habit -> viewModel.openHabitDetail(habit) },
                                onOpenDayNote = { date -> viewModel.openDayNoteEditor(date) },
                                onAddHabitClick = { viewModel.openHabitEditor(null, isNew = true) }
                            )
                        }
                        ScreenDestination.WEEKLY_REVIEW -> {
                            WeeklyReviewScreen(
                                state = uiState,
                                onSaveReflection = { reflection -> viewModel.saveWeeklyReflection(reflection) }
                            )
                        }
                        ScreenDestination.HISTORY -> {
                            HistoryScreen(
                                state = uiState,
                                selectedWeek = null,
                                onSelectWeek = { viewModel.openHistoricalWeek(it) },
                                onCloseDetail = { viewModel.closeHistoricalWeek() }
                            )
                        }
                        ScreenDestination.ANALYTICS -> {
                            AnalyticsScreen(
                                state = uiState,
                                onWeekClick = { viewModel.openHistoricalWeek(it) }
                            )
                        }
                        ScreenDestination.HABITS -> {
                            HabitsScreen(
                                state = uiState,
                                onAddHabitClick = { viewModel.openHabitEditor(null, isNew = true) },
                                onEditHabit = { habit -> viewModel.openHabitEditor(habit, isNew = false) },
                                onToggleActive = { id, active -> viewModel.toggleHabitActive(id, active) },
                                onMoveOrder = { id, dir -> viewModel.moveHabitOrder(id, dir) },
                                onDeleteHabit = { id -> viewModel.deleteHabit(id) },
                                onHabitClick = { habit -> viewModel.openHabitDetail(habit) }
                            )
                        }
                        ScreenDestination.SETTINGS -> {
                            SettingsScreen(
                                state = uiState,
                                onNavigateToHistory = { viewModel.navigateTo(ScreenDestination.HISTORY) },
                                onExportBackup = { onReady -> viewModel.exportBackupJson(onReady) },
                                onImportBackup = { json -> viewModel.importBackupJson(json) },
                                onResetAllData = { viewModel.resetAllData() }
                            )
                        }
                    }
                }
            }

            // Cell Edit Dialog
            editingCell?.let { (record, habit) ->
                CellEditDialog(
                    record = record,
                    habit = habit,
                    onDismiss = { viewModel.closeCellEditor() },
                    onSave = { recordId, isCompleted, value, status ->
                        viewModel.updateCellRecord(recordId, isCompleted, value, status)
                    }
                )
            }

            // Day Note Dialog
            editingDayNote?.let { dayNote ->
                DayNoteDialog(
                    dayNote = dayNote,
                    onDismiss = { viewModel.closeDayNoteEditor() },
                    onSave = { note -> viewModel.saveDayNote(note) }
                )
            }

            // Habit Edit / Create Dialog
            editingHabit?.let { habit ->
                HabitEditDialog(
                    habit = habit,
                    isNew = isCreatingNewHabit,
                    onDismiss = { viewModel.closeHabitEditor() },
                    onSave = { savedHabit -> viewModel.saveHabit(savedHabit) }
                )
            }
        }
    }
}
