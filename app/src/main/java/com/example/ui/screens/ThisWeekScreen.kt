package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRecordEntity
import com.example.data.model.HabitSnapshot
import com.example.domain.calculator.DateHelper
import com.example.ui.components.DashboardSummaryRow
import com.example.ui.components.HabitTrackerTable
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TodayHighlight
import com.example.ui.viewmodel.HabitUiState

@Composable
fun ThisWeekScreen(
    state: HabitUiState,
    onCellClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onCellLongClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onHabitClick: (habit: HabitSnapshot) -> Unit,
    onOpenDayNote: (date: String) -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EmeraldSuccess)
        }
        return
    }

    val currentMonday = DateHelper.getMondayOfWeek(state.todayDate)
    val dateRangeDisplay = DateHelper.getWeekDisplayRange(currentMonday)
    val summary = state.currentWeekSummary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Header Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "THIS WEEK",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Compact horizontal Today badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TodayHighlight.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TodayHighlight.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TodayHighlight)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Today: ${DateHelper.getDayShortName(DateHelper.getDayOfWeek(state.todayDate))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TodayHighlight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateRangeDisplay,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Stats Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Overall Completion",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${summary?.overallPercentage ?: 0}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldSuccess
                                )
                                val prevScore = summary?.comparison?.previousScore
                                if (prevScore != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Prev week: $prevScore%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    val delta = summary.comparison.scoreDelta
                                    val deltaStr = if (delta >= 0) "+$delta%" else "$delta%"
                                    val deltaColor = if (delta >= 0) EmeraldSuccess else AmberAttention
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "($deltaStr)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = deltaColor
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Target Pace",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${summary?.targetAdherenceCount ?: 0} / ${summary?.totalHabitsCount ?: 0} on target",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { ((summary?.overallPercentage ?: 0) / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = EmeraldSuccess,
                        trackColor = SurfaceElevated,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // 4 Compact Dashboard Summary Cards
        item {
            DashboardSummaryRow(summary = summary)
        }

        // Daily Notes Quick Access Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Daily Notes",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY CONTEXT & NOTES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Tap day to log mood / energy / sleep",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 1..7) {
                            val dateForDay = DateHelper.getDateForDayOfWeek(currentMonday, d)
                            val isToday = DateHelper.isToday(dateForDay, state.todayDate)
                            val hasNote = state.currentWeekNotes.any { it.date == dateForDay && (it.note != null || it.mood != null) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isToday) TodayHighlight.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(
                                        width = if (isToday) 1.5.dp else 0.5.dp,
                                        color = if (isToday) TodayHighlight else TableBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onOpenDayNote(dateForDay) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = DateHelper.getDayShortName(d),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) TodayHighlight else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (hasNote) ElectricBlue else if (isToday) TodayHighlight else TableBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Habit Table Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WEEKLY HABIT TRACKER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Click day cell to complete · Tap habit name for history deep-dive",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // The Habit Table
        item {
            HabitTrackerTable(
                habitProgressList = state.currentWeekProgress,
                mondayDate = currentMonday,
                todayDate = state.todayDate,
                onCellClick = onCellClick,
                onCellLongClick = onCellLongClick,
                onHabitClick = onHabitClick
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
