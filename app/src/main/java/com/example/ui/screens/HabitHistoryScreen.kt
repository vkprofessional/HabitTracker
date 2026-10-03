package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitSnapshot
import com.example.domain.calculator.DateHelper
import com.example.domain.calculator.HabitCalculator
import com.example.ui.components.HabitTypeBadge
import com.example.ui.components.SummaryStatCard
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HabitUiState
import kotlin.math.roundToInt

@Composable
fun HabitHistoryScreen(
    habit: HabitSnapshot,
    state: HabitUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitRecords = remember(state.allRecords, habit.id) {
        state.allRecords.filter { it.habitId == habit.id }
    }

    // Streaks
    val streaks = remember(habitRecords, habit) {
        HabitCalculator.calculateHabitStreaks(habit.id, habit, state.allRecords)
    }
    val currentStreak = streaks.first
    val longestStreak = streaks.second

    // Current week progress
    val currentWeekProg = remember(habit, state.currentWeekRecords) {
        val currRecs = state.currentWeekRecords.filter { it.habitId == habit.id }
        HabitCalculator.calculateHabitProgress(habit, currRecs, state.todayDate)
    }

    // Previous week progress
    val prevWeekProg = remember(habit, state.allRecords, state.allWeeks) {
        val prevMonday = DateHelper.getPreviousMonday(DateHelper.getMondayOfWeek(state.todayDate))
        val prevWeekId = DateHelper.getWeekId(prevMonday)
        val pRecs = state.allRecords.filter { it.weekId == prevWeekId && it.habitId == habit.id }
        if (pRecs.isNotEmpty()) {
            HabitCalculator.calculateHabitProgress(habit, pRecs, state.todayDate)
        } else null
    }

    // Day of week distribution
    val dayPerf = remember(habitRecords, habit) {
        val habitsMap = mapOf(habit.id to habit)
        HabitCalculator.calculateDayPerformance(habitRecords, habitsMap)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.icon, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HabitTypeBadge(type = habit.trackingType)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Target: ${habit.weeklyTarget}d/wk (${habit.dailyTarget} ${habit.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Key stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Current Week",
                    value = "${currentWeekProg.completedDays}/${currentWeekProg.targetDays}",
                    subtitle = "${(currentWeekProg.successRate * 100).roundToInt()}% score",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Previous Week",
                    value = if (prevWeekProg != null) "${prevWeekProg.completedDays}/${prevWeekProg.targetDays}" else "—",
                    subtitle = if (prevWeekProg != null) "${(prevWeekProg.successRate * 100).roundToInt()}% score" else "No record",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Active Streak",
                    value = "$currentStreak days",
                    subtitle = "Consecutive completion",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Longest Streak",
                    value = "$longestStreak days",
                    subtitle = "All-time personal best",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Day of Week Performance
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAY-OF-WEEK DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Success percentage for ${habit.name} by day",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 1..7) {
                            val rate = dayPerf[d] ?: 0
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$rate%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rate >= 70) EmeraldSuccess else if (rate >= 40) ElectricBlue else AmberAttention
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceElevated),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(((rate / 100f) * 70).dp.coerceAtLeast(3.dp))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (rate >= 70) EmeraldSuccess else if (rate >= 40) ElectricBlue else AmberAttention)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = DateHelper.getDayShortName(d),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent chronological logs
        item {
            Text(
                text = "RECENT COMPLETION LOGS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        val sortedRecords = habitRecords.sortedByDescending { it.date }.take(21)
        if (sortedRecords.isEmpty()) {
            item {
                Text("No logged entries for this habit yet.", color = TextMuted)
            }
        } else {
            items(sortedRecords.size) { idx ->
                val rec = sortedRecords[idx]
                val success = HabitCalculator.isDaySuccess(habit.trackingType, habit.dailyTarget, rec)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, TableBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = rec.date,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${DateHelper.getDayShortName(rec.dayOfWeek)} · Week ${rec.weekId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (success) EmeraldSuccess.copy(alpha = 0.2f) else SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (success) "Completed" else "Not Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (success) EmeraldSuccess else TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
