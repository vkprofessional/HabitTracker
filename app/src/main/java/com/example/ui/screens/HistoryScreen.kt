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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.model.WeekEntity
import com.example.data.model.WeeklyReflection
import com.example.domain.calculator.DateHelper
import com.example.domain.calculator.HabitCalculator
import com.example.ui.components.HabitTrackerTable
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HabitUiState
import org.json.JSONArray
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    state: HabitUiState,
    selectedWeek: WeekEntity?,
    onSelectWeek: (WeekEntity) -> Unit,
    onCloseDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedWeek != null) {
        HistoricalSnapshotDetail(
            week = selectedWeek,
            allRecords = state.allRecords,
            todayDate = state.todayDate,
            onBack = onCloseDetail,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "HISTORICAL WEEKS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Immutable weekly snapshots preserved over months",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (state.allWeeks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No historical data yet.", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Each completed week will be automatically preserved here as a permanent historical snapshot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(state.allWeeks.size) { idx ->
                val week = state.allWeeks[idx]
                val prevWeek = if (idx + 1 < state.allWeeks.size) state.allWeeks[idx + 1] else null
                HistoricalWeekCard(
                    week = week,
                    prevWeek = prevWeek,
                    allRecords = state.allRecords,
                    todayDate = state.todayDate,
                    onClick = { onSelectWeek(week) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun HistoricalWeekCard(
    week: WeekEntity,
    prevWeek: WeekEntity?,
    allRecords: List<com.example.data.model.DailyRecordEntity>,
    todayDate: String,
    onClick: () -> Unit
) {
    val weekRecords = allRecords.filter { it.weekId == week.weekId }
    val snapshots = remember(week.habitSnapshotsJson) {
        parseSnapshots(week.habitSnapshotsJson)
    }

    val progresses = remember(snapshots, weekRecords) {
        snapshots.map { HabitCalculator.calculateHabitProgress(it, weekRecords.filter { r -> r.habitId == it.id }, todayDate) }
    }

    val score = remember(progresses, weekRecords) {
        HabitCalculator.calculateWeeklyProgress(progresses, weekRecords, todayDate)
    }

    val prevScore = remember(prevWeek, allRecords) {
        if (prevWeek == null) null else {
            val pRecords = allRecords.filter { it.weekId == prevWeek.weekId }
            val pSnaps = parseSnapshots(prevWeek.habitSnapshotsJson)
            val pProgs = pSnaps.map { HabitCalculator.calculateHabitProgress(it, pRecords.filter { r -> r.habitId == it.id }, todayDate) }
            HabitCalculator.calculateWeeklyProgress(pProgs, pRecords, todayDate)
        }
    }

    val delta = if (prevScore != null) score - prevScore else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("historical_week_${week.weekId}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (week.isArchived) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Archived Snapshot",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = DateHelper.getWeekDisplayRange(week.startDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${snapshots.size} habits tracked · ${week.weekId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 70) EmeraldSuccess else if (score >= 50) ElectricBlue else AmberAttention
                    )
                    if (delta != null) {
                        val deltaStr = if (delta >= 0) "+$delta%" else "$delta%"
                        Text(
                            text = deltaStr,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (delta >= 0) EmeraldSuccess else AmberAttention
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Snapshot",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HistoricalSnapshotDetail(
    week: WeekEntity,
    allRecords: List<com.example.data.model.DailyRecordEntity>,
    todayDate: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekRecords = allRecords.filter { it.weekId == week.weekId }
    val snapshots = remember(week.habitSnapshotsJson) { parseSnapshots(week.habitSnapshotsJson) }
    val progresses = remember(snapshots, weekRecords) {
        snapshots.map { HabitCalculator.calculateHabitProgress(it, weekRecords.filter { r -> r.habitId == it.id }, todayDate) }
    }
    val score = remember(progresses, weekRecords) { HabitCalculator.calculateWeeklyProgress(progresses, weekRecords, todayDate) }
    val reflection = remember(week.reflectionJson) { WeeklyReflection.fromJson(week.reflectionJson) }

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
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "HISTORICAL SNAPSHOT",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = DateHelper.getWeekDisplayRange(week.startDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Snapshot Stats Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Completion Score", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "$score%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Immutable Record",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Habit Snapshot Table
        item {
            Text(
                text = "SAVED HABIT MATRIX",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            HabitTrackerTable(
                habitProgressList = progresses,
                mondayDate = week.startDate,
                todayDate = todayDate,
                onCellClick = { _, _ -> },
                onCellLongClick = { _, _ -> },
                onHabitClick = { }
            )
        }

        // Saved Reflection
        if (reflection.whatWentWell.isNotBlank() || reflection.whatWentBadly.isNotBlank() || reflection.mainPriority.isNotBlank()) {
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
                            text = "SAVED REFLECTION FOR THIS WEEK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberAttention
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (reflection.whatWentWell.isNotBlank()) {
                            Text("What went well:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(reflection.whatWentWell, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (reflection.whatWentBadly.isNotBlank()) {
                            Text("What went badly:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(reflection.whatWentBadly, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (reflection.whyMissedHabits.isNotBlank()) {
                            Text("Why missed habits:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(reflection.whyMissedHabits, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (reflection.mainPriority.isNotBlank()) {
                            Text("Main priority was:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(reflection.mainPriority, style = MaterialTheme.typography.bodyMedium, color = ElectricBlue)
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

private fun parseSnapshots(jsonString: String): List<HabitSnapshot> {
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
