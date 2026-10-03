package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitSnapshot
import com.example.data.model.WeekEntity
import com.example.domain.calculator.DateHelper
import com.example.domain.calculator.HabitCalculator
import com.example.ui.components.SummaryStatCard
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CoralError
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

enum class AnalyticsRange(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    YEAR("Year")
}

data class WeekScorePoint(
    val weekId: String,
    val label: String,
    val score: Int
)

@Composable
fun AnalyticsScreen(
    state: HabitUiState,
    onWeekClick: (WeekEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(AnalyticsRange.WEEK) }

    val habitsSnapshots = remember(state.activeHabits) {
        state.activeHabits.map { HabitSnapshot.fromEntity(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ANALYTICS & TRENDS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Analyze consistency and performance across time ranges",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Lightweight Time-Range Selector: [ Day ] [ Week ] [ Month ] [ 3M ] [ 6M ] [ Year ]
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnalyticsRange.values().forEach { range ->
                    val isSelected = selectedRange == range
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedRange = range }
                            .testTag("range_pill_${range.label}"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldSuccessContainer else CardSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldSuccess else TableBorder
                        )
                    ) {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) EmeraldSuccess else TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Content based on selected range
        when (selectedRange) {
            AnalyticsRange.DAY -> {
                dayAnalyticsSection(state, habitsSnapshots)
            }
            AnalyticsRange.WEEK -> {
                weekAnalyticsSection(state, habitsSnapshots)
            }
            AnalyticsRange.MONTH -> {
                monthAnalyticsSection(state, habitsSnapshots)
            }
            AnalyticsRange.THREE_MONTHS -> {
                multiMonthAnalyticsSection(state, habitsSnapshots, monthsBack = 3, rangeTitle = "3 MONTHS PERFORMANCE")
            }
            AnalyticsRange.SIX_MONTHS -> {
                multiMonthAnalyticsSection(state, habitsSnapshots, monthsBack = 6, rangeTitle = "6 MONTHS PERFORMANCE")
            }
            AnalyticsRange.YEAR -> {
                yearAnalyticsSection(state, habitsSnapshots)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 1. DAY-WISE ANALYTICS SECTION
 */
private fun androidx.compose.foundation.lazy.LazyListScope.dayAnalyticsSection(
    state: HabitUiState,
    habitsSnapshots: List<HabitSnapshot>
) {
    item {
        val habitsMap = remember(habitsSnapshots) { habitsSnapshots.associateBy { it.id } }
        val dayPerf = remember(state.allRecords, habitsMap) {
            HabitCalculator.calculateDayPerformance(state.allRecords, habitsMap)
        }
        val bestDayNum = dayPerf.maxByOrNull { it.value }?.key ?: 1
        val weakestDayNum = dayPerf.minByOrNull { it.value }?.key ?: 1

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Peak Day",
                    value = DateHelper.getDayShortName(bestDayNum),
                    subtitle = "${dayPerf[bestDayNum] ?: 0}% completion",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Lowest Day",
                    value = DateHelper.getDayShortName(weakestDayNum),
                    subtitle = "${dayPerf[weakestDayNum] ?: 0}% completion",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAY-OF-WEEK PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Historical completion rate aggregated by weekday",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (d in 1..7) {
                            val rate = dayPerf[d] ?: 0
                            val dayName = DateHelper.getDayShortName(d)

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$rate%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rate >= 75) EmeraldSuccess else if (rate >= 50) ElectricBlue else AmberAttention
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceElevated),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(((rate / 100f) * 100).dp.coerceAtLeast(4.dp))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (rate >= 75) EmeraldSuccess else if (rate >= 50) ElectricBlue else AmberAttention
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Streaks
            HabitStreaksCard(habitsSnapshots = habitsSnapshots, allRecords = state.allRecords)
        }
    }
}

/**
 * 2. WEEK-WISE ANALYTICS SECTION
 */
private fun androidx.compose.foundation.lazy.LazyListScope.weekAnalyticsSection(
    state: HabitUiState,
    habitsSnapshots: List<HabitSnapshot>
) {
    item {
        val weekScores = remember(state.allWeeks, state.allRecords) {
            state.allWeeks.reversed().map { week ->
                val weekRecords = state.allRecords.filter { it.weekId == week.weekId }
                val snaps = parseSnapshots(week.habitSnapshotsJson)
                val progs = snaps.map { HabitCalculator.calculateHabitProgress(it, weekRecords.filter { r -> r.habitId == it.id }, state.todayDate) }
                val score = HabitCalculator.calculateWeeklyProgress(progs, weekRecords, state.todayDate)
                val monMonth = try {
                    val parts = week.startDate.split("-")
                    "${parts[1]}/${parts[2]}"
                } catch (e: Exception) {
                    week.weekId
                }
                WeekScorePoint(week.weekId, monMonth, score)
            }
        }

        val fourWeekAvg = remember(weekScores) {
            val last4 = weekScores.takeLast(4)
            if (last4.isNotEmpty()) (last4.map { it.score }.average()).roundToInt() else 0
        }

        val eightWeekAvg = remember(weekScores) {
            val last8 = weekScores.takeLast(8)
            if (last8.isNotEmpty()) (last8.map { it.score }.average()).roundToInt() else 0
        }

        val bestWeek = remember(weekScores) { weekScores.maxByOrNull { it.score } }
        val worstWeek = remember(weekScores) { weekScores.minByOrNull { it.score } }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "4-Week Average",
                    value = "$fourWeekAvg%",
                    subtitle = "Short-term trend",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "8-Week Average",
                    value = "$eightWeekAvg%",
                    subtitle = "Long-term baseline",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Best Week",
                    value = if (bestWeek != null) "${bestWeek.score}%" else "—",
                    subtitle = bestWeek?.label,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Lowest Week",
                    value = if (worstWeek != null) "${worstWeek.score}%" else "—",
                    subtitle = worstWeek?.label,
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Completion Trend Chart
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
                        Text(
                            text = "WEEKLY COMPLETION TREND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Score", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4-Wk Avg ($fourWeekAvg%)", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (weekScores.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No historical weeks tracked yet", color = TextMuted)
                        }
                    } else {
                        WeeklyTrendChart(
                            points = weekScores,
                            fourWeekAvg = fourWeekAvg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            HabitStreaksCard(habitsSnapshots = habitsSnapshots, allRecords = state.allRecords)

            // Pattern Insights
            if (state.patternInsights.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AmberAttention,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERSONAL PATTERNS DETECTED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAttention,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        state.patternInsights.forEach { pattern ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = pattern.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pattern.isPositive) EmeraldSuccess else AmberAttention
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = pattern.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. MONTHLY ANALYTICS SECTION
 */
private fun androidx.compose.foundation.lazy.LazyListScope.monthAnalyticsSection(
    state: HabitUiState,
    habitsSnapshots: List<HabitSnapshot>
) {
    item {
        val initialMonth = remember(state.todayDate) { state.todayDate.substring(0, 7) }
        var currentMonthKey by remember { mutableStateOf(initialMonth) }

        val monthlyStats = remember(currentMonthKey, state.allRecords, habitsSnapshots) {
            HabitCalculator.calculateMonthlyStats(
                targetYearMonth = currentMonthKey,
                allRecords = state.allRecords,
                habits = habitsSnapshots,
                todayDate = state.todayDate
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Month navigation selector: [<] September 2026 [>]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonthKey = DateHelper.getPreviousMonthKey(currentMonthKey)
                    }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = monthlyStats.monthDisplayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = {
                        currentMonthKey = DateHelper.getNextMonthKey(currentMonthKey)
                    }) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Stat Cards 1: Overall Completion & Month-over-Month Delta
            val deltaStr = monthlyStats.deltaFromPrevious?.let {
                if (it >= 0) "+$it%" else "$it%"
            }
            val isPos = monthlyStats.deltaFromPrevious?.let { it >= 0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Monthly Completion",
                    value = "${monthlyStats.overallPercentage}%",
                    subtitle = monthlyStats.previousMonthPercentage?.let { "Prev month: $it%" } ?: "First tracked month",
                    delta = deltaStr,
                    isPositiveDelta = isPos,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Habit Volume",
                    value = "${monthlyStats.totalSuccessfulHabitDays}",
                    subtitle = "Avg ${String.format("%.1f", monthlyStats.averageDailyCompleted)} habits/day",
                    modifier = Modifier.weight(1f)
                )
            }

            // Stat Cards 2: Best & Weakest Habit in Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Best Habit",
                    value = monthlyStats.bestHabit?.let { "${it.second}%" } ?: "—",
                    subtitle = monthlyStats.bestHabit?.first?.name,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Weakest Habit",
                    value = monthlyStats.weakestHabit?.let { "${it.second}%" } ?: "—",
                    subtitle = monthlyStats.weakestHabit?.first?.name,
                    modifier = Modifier.weight(1f)
                )
            }

            // Stat Cards 3: Best & Weakest Day of Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Best Day",
                    value = monthlyStats.bestDay?.let { "${it.second}%" } ?: "—",
                    subtitle = monthlyStats.bestDay?.first,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Weakest Day",
                    value = monthlyStats.weakestDay?.let { "${it.second}%" } ?: "—",
                    subtitle = monthlyStats.weakestDay?.first,
                    modifier = Modifier.weight(1f)
                )
            }

            // Habit Consistency Breakdown in Month
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONTHLY HABIT BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (monthlyStats.habitBreakdown.isEmpty()) {
                        Text("No habit logs recorded for this month", color = TextMuted, fontSize = 13.sp)
                    } else {
                        monthlyStats.habitBreakdown.forEach { (habit, pct) ->
                            val barColor = when {
                                pct >= 80 -> EmeraldSuccess
                                pct >= 50 -> ElectricBlue
                                else -> AmberAttention
                            }
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(habit.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = barColor,
                                    trackColor = SurfaceElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. MULTI-MONTH ANALYTICS SECTION (3M and 6M)
 */
private fun androidx.compose.foundation.lazy.LazyListScope.multiMonthAnalyticsSection(
    state: HabitUiState,
    habitsSnapshots: List<HabitSnapshot>,
    monthsBack: Int,
    rangeTitle: String
) {
    item {
        val stats = remember(monthsBack, state.allRecords, habitsSnapshots) {
            HabitCalculator.calculateMultiPeriodStats(
                monthsBack = monthsBack,
                allRecords = state.allRecords,
                habits = habitsSnapshots,
                todayDate = state.todayDate
            )
        }

        val deltaStr = if (stats.trendDelta >= 0) "+${stats.trendDelta}%" else "${stats.trendDelta}%"
        val isPos = stats.trendDelta >= 0

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Average Completion",
                    value = "${stats.averageCompletion}%",
                    subtitle = "${stats.totalSuccessfulDays} successful habit-days",
                    delta = deltaStr,
                    isPositiveDelta = isPos,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Trend Direction",
                    value = if (stats.trendDelta >= 0) "Improving" else "Declining",
                    subtitle = "Recent vs prior period",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Top Performer",
                    value = stats.bestHabit?.let { "${it.second}%" } ?: "—",
                    subtitle = stats.bestHabit?.first?.name,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Lowest Performer",
                    value = stats.weakestHabit?.let { "${it.second}%" } ?: "—",
                    subtitle = stats.weakestHabit?.first?.name,
                    modifier = Modifier.weight(1f)
                )
            }

            // Habit Rankings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "$rangeTitle RANKINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (stats.habitRankings.isEmpty()) {
                        Text("No habit logs available in this time window", color = TextMuted)
                    } else {
                        stats.habitRankings.forEach { (habit, pct) ->
                            val barColor = when {
                                pct >= 80 -> EmeraldSuccess
                                pct >= 50 -> ElectricBlue
                                else -> AmberAttention
                            }
                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(habit.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = barColor,
                                    trackColor = SurfaceElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. YEAR-WISE ANALYTICS SECTION
 */
private fun androidx.compose.foundation.lazy.LazyListScope.yearAnalyticsSection(
    state: HabitUiState,
    habitsSnapshots: List<HabitSnapshot>
) {
    item {
        val currentYear = remember(state.todayDate) { state.todayDate.take(4) }
        val yearlyStats = remember(currentYear, state.allRecords, habitsSnapshots) {
            HabitCalculator.calculateYearlyStats(
                targetYear = currentYear,
                allRecords = state.allRecords,
                habits = habitsSnapshots,
                todayDate = state.todayDate
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Year $currentYear Overall",
                    value = "${yearlyStats.overallPercentage}%",
                    subtitle = "Annual habit completion",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Months Tracked",
                    value = "${yearlyStats.monthlyTrend.size}",
                    subtitle = "Active periods",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = "Best Month",
                    value = yearlyStats.bestMonth?.let { "${it.second}%" } ?: "—",
                    subtitle = yearlyStats.bestMonth?.first,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Weakest Month",
                    value = yearlyStats.weakestMonth?.let { "${it.second}%" } ?: "—",
                    subtitle = yearlyStats.weakestMonth?.first,
                    modifier = Modifier.weight(1f)
                )
            }

            // Month-by-month progress bars
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONTH-BY-MONTH TREND ($currentYear)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (yearlyStats.monthlyTrend.isEmpty()) {
                        Text("No logs recorded for $currentYear yet", color = TextMuted)
                    } else {
                        yearlyStats.monthlyTrend.forEach { (monthName, pct) ->
                            val barColor = when {
                                pct >= 80 -> EmeraldSuccess
                                pct >= 50 -> ElectricBlue
                                else -> AmberAttention
                            }
                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = barColor,
                                    trackColor = SurfaceElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }

            // Long-Term Habit Consistency
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LONG-TERM HABIT CONSISTENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    yearlyStats.habitConsistency.forEach { (habit, pct) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(habit.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "$pct% annual",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 75) EmeraldSuccess else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitStreaksCard(
    habitsSnapshots: List<HabitSnapshot>,
    allRecords: List<com.example.data.model.DailyRecordEntity>
) {
    val habitStreaks = remember(habitsSnapshots, allRecords) {
        habitsSnapshots.map { habit ->
            val streaks = HabitCalculator.calculateHabitStreaks(habit.id, habit, allRecords)
            Triple(habit, streaks.first, streaks.second)
        }.sortedByDescending { it.third }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = AmberAttention,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HABIT STREAKS & CONSISTENCY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberAttention,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            habitStreaks.take(8).forEach { (habit, current, longest) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(habit.icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Current: $current d",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (current > 0) EmeraldSuccess else TextMuted
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Best: $longest d",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTrendChart(
    points: List<WeekScorePoint>,
    fourWeekAvg: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingBottom = 20.dp.toPx()
        val graphHeight = height - paddingBottom

        if (points.isEmpty()) return@Canvas

        val y100 = 0f
        val y50 = graphHeight / 2f
        val y0 = graphHeight

        drawLine(Color(0xFF262D3D), Offset(0f, y100), Offset(width, y100), strokeWidth = 1f)
        drawLine(Color(0xFF262D3D), Offset(0f, y50), Offset(width, y50), strokeWidth = 1f)
        drawLine(Color(0xFF262D3D), Offset(0f, y0), Offset(width, y0), strokeWidth = 1f)

        val avgY = graphHeight - (graphHeight * (fourWeekAvg / 100f))
        drawLine(
            color = Color(0xFF58A6FF).copy(alpha = 0.5f),
            start = Offset(0f, avgY),
            end = Offset(width, avgY),
            strokeWidth = 2f
        )

        val stepX = if (points.size > 1) width / (points.size - 1) else width / 2f
        val path = Path()

        val renderedOffsets = points.mapIndexed { idx, pt ->
            val x = if (points.size > 1) idx * stepX else width / 2f
            val y = graphHeight - (graphHeight * (pt.score.coerceIn(0, 100) / 100f))
            Offset(x, y)
        }

        renderedOffsets.forEachIndexed { i, offset ->
            if (i == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }

        drawPath(
            path = path,
            color = Color(0xFF3FB950),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        renderedOffsets.forEach { offset ->
            drawCircle(
                color = Color(0xFF0B0F17),
                radius = 5.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color(0xFF3FB950),
                radius = 3.5.dp.toPx(),
                center = offset
            )
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
