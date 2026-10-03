package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayNoteEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitEntity
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.data.model.WeeklyInsightSummary
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CellBorder
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
import com.example.ui.theme.TodayHighlight
import kotlin.math.roundToInt

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    delta: String? = null,
    isPositiveDelta: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (delta != null) {
                    val badgeBg = if (isPositiveDelta == true) EmeraldSuccessContainer else AmberContainer
                    val badgeColor = if (isPositiveDelta == true) EmeraldSuccess else CoralError
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = delta,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DashboardSummaryRow(
    summary: WeeklyInsightSummary?,
    modifier: Modifier = Modifier
) {
    if (summary == null) return

    val deltaStr = summary.comparison?.let {
        if (it.scoreDelta >= 0) "+${it.scoreDelta}%" else "${it.scoreDelta}%"
    }
    val isPos = summary.comparison?.let { it.scoreDelta >= 0 }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Weekly Overview & Delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEK PERFORMANCE OVERVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (deltaStr != null) {
                    val badgeBg = if (isPos == true) EmeraldSuccessContainer else AmberContainer
                    val badgeColor = if (isPos == true) EmeraldSuccess else CoralError
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$deltaStr vs prev",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            // Stat 1: Overall Progress & Pacing
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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${summary.overallPercentage}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldSuccess
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Overall Weekly Pacing",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${summary.targetAdherenceCount} of ${summary.totalHabitsCount} habits currently on target",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Stat 2: Targets Met & Consistency
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
                    Text("🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Targets Adherence",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Weekly goal completion",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "${summary.targetAdherenceCount} / ${summary.totalHabitsCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
            }

            // Stat 3: Best Habit
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Top Consistent Habit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Highest success rate this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = summary.bestHabit ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            }

            // Stat 4: Peak Adherence Day
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Strongest Day",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Peak aggregate completions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = summary.bestDay ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberAttention
                )
            }
        }
    }
}

@Composable
fun HabitTypeBadge(
    type: HabitType,
    modifier: Modifier = Modifier
) {
    val (label, bg, fg) = when (type) {
        HabitType.BOOLEAN -> Triple("Check", Color(0xFF1E2838), ElectricBlue)
        HabitType.NUMERIC -> Triple("Numeric", Color(0xFF262D22), EmeraldSuccess)
        HabitType.DURATION -> Triple("Duration", Color(0xFF332B1A), AmberAttention)
        HabitType.LIMIT -> Triple("Limit", Color(0xFF341F28), CoralError)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
fun TrendBadge(
    trend: String,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (trend.lowercase()) {
        "improved" -> Triple(Icons.Default.TrendingUp, EmeraldSuccess, "Improved")
        "declined" -> Triple(Icons.Default.TrendingDown, CoralError, "Declined")
        else -> Triple(Icons.Default.TrendingFlat, TextSecondary, "Stable")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Dialog for editing or logging a cell's detailed value (numeric, duration, limit or status).
 */
@Composable
fun CellEditDialog(
    record: DailyRecordEntity,
    habit: HabitSnapshot,
    onDismiss: () -> Unit,
    onSave: (recordId: String, isCompleted: Boolean, value: Float?, status: DayStatus) -> Unit
) {
    var isCompleted by remember { mutableStateOf(record.isCompleted) }
    var numericText by remember { mutableStateOf(record.numericValue?.toString() ?: habit.dailyTarget.toString()) }
    var selectedStatus by remember { mutableStateOf(record.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Day ${record.dayOfWeek} (${record.date})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Habit Type: ${habit.trackingType.name} (Target: ${habit.dailyTarget} ${habit.unit})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (habit.trackingType == HabitType.BOOLEAN) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCompleted) EmeraldSuccessContainer else SurfaceElevated)
                            .clickable {
                                isCompleted = !isCompleted
                                selectedStatus = if (isCompleted) DayStatus.COMPLETED else DayStatus.NOT_COMPLETED
                            }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCompleted) EmeraldSuccess else TableBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = ObsidianBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isCompleted) "Completed" else "Not Completed",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) EmeraldSuccess else TextPrimary
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = numericText,
                        onValueChange = { numericText = it },
                        label = { Text("Logged Value (${habit.unit.ifBlank { "amount" }})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("numeric_value_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (habit.trackingType) {
                            HabitType.LIMIT -> "Rule: Logged amount <= ${habit.dailyTarget} ${habit.unit} counts as success."
                            HabitType.DURATION -> "Rule: Logged duration >= ${habit.dailyTarget} ${habit.unit} counts as success."
                            else -> "Rule: Logged value >= ${habit.dailyTarget} ${habit.unit} counts as success."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Status override:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statuses = listOf(DayStatus.COMPLETED, DayStatus.NOT_COMPLETED, DayStatus.NOT_APPLICABLE)
                    statuses.forEach { st ->
                        val isSel = selectedStatus == st
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) ElectricBlue else SurfaceElevated)
                                .clickable {
                                    selectedStatus = st
                                    if (st == DayStatus.COMPLETED) isCompleted = true
                                    if (st == DayStatus.NOT_COMPLETED || st == DayStatus.NOT_APPLICABLE) isCompleted = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (st) {
                                    DayStatus.COMPLETED -> "Done"
                                    DayStatus.NOT_COMPLETED -> "Missed"
                                    DayStatus.NOT_APPLICABLE -> "N/A"
                                    else -> "Future"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) ObsidianBg else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedVal = numericText.toFloatOrNull()
                    val success = if (habit.trackingType == HabitType.BOOLEAN) {
                        isCompleted
                    } else if (habit.trackingType == HabitType.LIMIT) {
                        (parsedVal ?: 0f) <= habit.dailyTarget
                    } else {
                        (parsedVal ?: 0f) >= habit.dailyTarget
                    }

                    val finalStatus = when (selectedStatus) {
                        DayStatus.NOT_APPLICABLE -> DayStatus.NOT_APPLICABLE
                        else -> if (success) DayStatus.COMPLETED else DayStatus.NOT_COMPLETED
                    }

                    onSave(record.id, success, parsedVal, finalStatus)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("save_cell_button")
            ) {
                Text("Save", color = ObsidianBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}

/**
 * Dialog for editing daily note context (Mood, Energy, Sleep, Freeform note).
 */
@Composable
fun DayNoteDialog(
    dayNote: DayNoteEntity,
    onDismiss: () -> Unit,
    onSave: (DayNoteEntity) -> Unit
) {
    var mood by remember { mutableIntStateOf(dayNote.mood ?: 7) }
    var energy by remember { mutableIntStateOf(dayNote.energy ?: 7) }
    var sleepText by remember { mutableStateOf(dayNote.sleepHours?.toString() ?: "7.5") }
    var noteText by remember { mutableStateOf(dayNote.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Daily Context & Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = dayNote.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mood Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mood: $mood / 10", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text(
                        if (mood >= 8) "😊 Great" else if (mood >= 5) "😐 Okay" else "😔 Low",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberAttention
                    )
                }
                Slider(
                    value = mood.toFloat(),
                    onValueChange = { mood = it.roundToInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricBlue,
                        activeTrackColor = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Energy Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Energy: $energy / 10", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text(
                        if (energy >= 8) "⚡ High" else if (energy >= 5) "🔋 Steady" else "🪫 Drained",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberAttention
                    )
                }
                Slider(
                    value = energy.toFloat(),
                    onValueChange = { energy = it.roundToInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberAttention,
                        activeTrackColor = AmberAttention
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sleepText,
                    onValueChange = { sleepText = it },
                    label = { Text("Sleep Duration (hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Daily Reflection / Notes") },
                    placeholder = { Text("What happened today? Any triggers or breakthroughs?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        dayNote.copy(
                            mood = mood,
                            energy = energy,
                            sleepHours = sleepText.toFloatOrNull(),
                            note = noteText.ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("save_day_note_button")
            ) {
                Text("Save Note", color = ObsidianBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}
