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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitWeekProgress
import com.example.data.model.WeeklyReflection
import com.example.ui.components.SummaryStatCard
import com.example.ui.components.TrendBadge
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
import kotlin.math.roundToInt

@Composable
fun WeeklyReviewScreen(
    state: HabitUiState,
    onSaveReflection: (WeeklyReflection) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = state.currentWeekSummary
    val currentReflection = remember(state.currentWeekEntity) {
        WeeklyReflection.fromJson(state.currentWeekEntity?.reflectionJson)
    }

    var whatWentWell by remember(currentReflection) { mutableStateOf(currentReflection.whatWentWell) }
    var whatWentBadly by remember(currentReflection) { mutableStateOf(currentReflection.whatWentBadly) }
    var whyMissed by remember(currentReflection) { mutableStateOf(currentReflection.whyMissedHabits) }
    var whatToChange by remember(currentReflection) { mutableStateOf(currentReflection.whatToChangeNextWeek) }
    var mainPriority by remember(currentReflection) { mutableStateOf(currentReflection.mainPriority) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Insights,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WEEKLY REVIEW & AUDIT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = summary?.dateRange ?: "Current Week",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section 1: End of Week Key Performance Metrics
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
                        text = "WEEK PERFORMANCE SCORECARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mobile-first Vertical Scorecard Stack
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Overall Completion
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccessContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${summary?.overallPercentage ?: 0}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Overall Week Score",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Target Adherence: ${summary?.targetAdherenceCount ?: 0} / ${summary?.totalHabitsCount ?: 0} goals met",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Best Habit & Weakest Habit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Top Habit:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Text(
                                text = summary?.bestHabit ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Needs Attention:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Text(
                                text = summary?.weakestHabit ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberAttention
                            )
                        }

                        // Strongest Day & Weakest Day
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Peak Day:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Text(
                                text = summary?.bestDay ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💤", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lowest Day:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Text(
                                text = summary?.weakestDay ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Habit-day counts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Successful Habit-Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${summary?.totalSuccessfulHabitDays ?: 0} done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Missed Habit-Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${summary?.missedHabitDays ?: 0} missed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if ((summary?.missedHabitDays ?: 0) > 0) CoralError else TextSecondary
                            )
                        }
                    }

                    // Delta improved / declined
                    summary?.comparison?.let { comp ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Improved from prev: ${comp.habitsImproved.size} habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Declined from prev: ${comp.habitsDeclined.size} habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (comp.habitsDeclined.isNotEmpty()) CoralError else TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Habit-by-Habit Detailed Analysis
        item {
            Text(
                text = "HABIT-BY-HABIT AUDIT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Empirical data breakdown compared to previous week",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        items(state.currentWeekProgress.size) { idx ->
            val prog = state.currentWeekProgress[idx]
            val prevProg = state.previousWeekSummary?.habitProgressList?.find { it.habit.id == prog.habit.id }
            HabitReviewCard(prog = prog, prevProg = prevProg)
        }

        // Section 3: Next Week Improvement Suggestions
        item {
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
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXT WEEK PLANNING & RECOMMENDATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Data-backed suggestions only. Targets are NOT modified automatically; you choose whether to adopt them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    state.nextWeekSuggestions.forEach { suggestion ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(suggestion.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = suggestion.habitName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (suggestion.suggestedAction) {
                                                "Increase target" -> EmeraldSuccessContainer
                                                "Reduce target" -> AmberAttention.copy(alpha = 0.2f)
                                                "Review habit" -> CoralError.copy(alpha = 0.2f)
                                                else -> CardSurface
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = suggestion.suggestedAction,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (suggestion.suggestedAction) {
                                            "Increase target" -> EmeraldSuccess
                                            "Reduce target" -> AmberAttention
                                            "Review habit" -> CoralError
                                            else -> TextSecondary
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Logged ${suggestion.previousCompletion} of ${suggestion.previousTarget} days · Trend: ${suggestion.trend}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = suggestion.rationalReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Weekly Reflection Form
        item {
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
                            Icons.Default.RateReview,
                            contentDescription = null,
                            tint = AmberAttention,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WEEKLY REFLECTION & INTROSPECTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberAttention,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saved permanently with this historical week's archive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = whatWentWell,
                        onValueChange = { whatWentWell = it },
                        label = { Text("What went well this week?") },
                        placeholder = { Text("Wins, routines executed smoothly, energy highlights...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_what_went_well"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = whatWentBadly,
                        onValueChange = { whatWentBadly = it },
                        label = { Text("What went badly or felt difficult?") },
                        placeholder = { Text("Distractions, friction points, missed routines...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_what_went_badly"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = whyMissed,
                        onValueChange = { whyMissed = it },
                        label = { Text("Why did I miss habits?") },
                        placeholder = { Text("Specific triggers, scheduling issues, low energy...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_why_missed"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = whatToChange,
                        onValueChange = { whatToChange = it },
                        label = { Text("What should I change next week?") },
                        placeholder = { Text("Adjust routine time, lower target, eliminate trigger...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_what_to_change"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mainPriority,
                        onValueChange = { mainPriority = it },
                        label = { Text("Main priority for next week") },
                        placeholder = { Text("The single non-negotiable keystone habit...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reflection_main_priority"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSaveReflection(
                                WeeklyReflection(
                                    whatWentWell = whatWentWell,
                                    whatWentBadly = whatWentBadly,
                                    whyMissedHabits = whyMissed,
                                    whatToChangeNextWeek = whatToChange,
                                    mainPriority = mainPriority
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_reflection_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = ObsidianBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Weekly Reflection",
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBg
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun HabitReviewCard(
    prog: HabitWeekProgress,
    prevProg: HabitWeekProgress?
) {
    val habit = prog.habit
    val currPercent = (prog.successRate * 100).roundToInt()
    val prevPercent = prevProg?.let { (it.successRate * 100).roundToInt() }

    val trend = when {
        prevPercent == null -> "Stable"
        currPercent > prevPercent + 5 -> "Improved"
        currPercent < prevPercent - 5 -> "Declined"
        else -> "Stable"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = habit.name.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Target: ${prog.targetDays} days | Completed: ${prog.completedDays} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                TrendBadge(trend = trend)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Success rate: $currPercent%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (currPercent >= 75) EmeraldSuccess else if (currPercent >= 50) AmberAttention else CoralError
                )
                if (prevPercent != null) {
                    Text(
                        text = "Previous week: $prevPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { prog.successRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = if (currPercent >= 75) EmeraldSuccess else if (currPercent >= 50) ElectricBlue else AmberAttention,
                trackColor = SurfaceElevated,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
