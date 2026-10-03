package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.data.model.HabitWeekProgress
import com.example.domain.calculator.DateHelper
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CellBorder
import com.example.ui.theme.CellEmpty
import com.example.ui.theme.CellHover
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TodayHighlight
import kotlin.math.roundToInt

private val HabitColWidth = 145.dp
private val TargetColWidth = 56.dp
private val DayColWidth = 46.dp
private val ProgressColWidth = 84.dp
private val RowHeight = 52.dp
private val HeaderHeight = 44.dp

@Composable
fun HabitTrackerTable(
    habitProgressList: List<HabitWeekProgress>,
    mondayDate: String,
    todayDate: String,
    onCellClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onCellLongClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onHabitClick: (habit: HabitSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < 650.dp) {
            // Mobile-first layout: Thumb-friendly habit cards with full-width 7-day strip
            MobileHabitTrackerList(
                habitProgressList = habitProgressList,
                mondayDate = mondayDate,
                todayDate = todayDate,
                onCellClick = onCellClick,
                onCellLongClick = onCellLongClick,
                onHabitClick = onHabitClick
            )
        } else {
            // Desktop/Tablet layout: Full table with horizontal scroll if needed
            DesktopHabitTable(
                habitProgressList = habitProgressList,
                mondayDate = mondayDate,
                todayDate = todayDate,
                onCellClick = onCellClick,
                onCellLongClick = onCellLongClick,
                onHabitClick = onHabitClick
            )
        }
    }
}

/**
 * Mobile-First Habit Tracker List:
 * Thumb-friendly cards with 7-day strip. No horizontal scrolling, large touch targets (>= 44dp).
 */
@Composable
fun MobileHabitTrackerList(
    habitProgressList: List<HabitWeekProgress>,
    mondayDate: String,
    todayDate: String,
    onCellClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onCellLongClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onHabitClick: (habit: HabitSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        habitProgressList.forEach { progress ->
            MobileHabitCard(
                progress = progress,
                mondayDate = mondayDate,
                todayDate = todayDate,
                onCellClick = onCellClick,
                onCellLongClick = onCellLongClick,
                onHabitClick = onHabitClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MobileHabitCard(
    progress: HabitWeekProgress,
    mondayDate: String,
    todayDate: String,
    onCellClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onCellLongClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onHabitClick: (habit: HabitSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = progress.habit
    val percent = (progress.successRate * 100).roundToInt()
    val barColor = when {
        percent >= 100 -> EmeraldSuccess
        percent >= 60 -> ElectricBlue
        percent >= 30 -> AmberAttention
        else -> CoralError
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Emoji + Name + Badges + Progress stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = { onHabitClick(habit) }
                    )
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.category,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            HabitTypeBadge(type = habit.trackingType)
                        }
                    }
                }

                // Target and Progress info
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    val targetLabel = when (habit.trackingType) {
                        HabitType.LIMIT -> "<${habit.dailyTarget.toInt()}${habit.unit}/day"
                        HabitType.DURATION -> ">${habit.dailyTarget.toInt()}${habit.unit}/day"
                        else -> "${habit.weeklyTarget} days/wk"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${progress.completedDays}/${progress.targetDays}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = targetLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { progress.successRate },
                        modifier = Modifier
                            .width(72.dp)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = barColor,
                        trackColor = SurfaceElevated,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7-Day Strip (Mon..Sun) - Full width, equal distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (dayNum in 1..7) {
                    val dateForDay = DateHelper.getDateForDayOfWeek(mondayDate, dayNum)
                    val isToday = DateHelper.isToday(dateForDay, todayDate)
                    val dayName = DateHelper.getDayShortName(dayNum)
                    val dayOfMonth = try {
                        dateForDay.split("-").last()
                    } catch (e: Exception) {
                        "$dayNum"
                    }

                    val record = progress.dayRecords.find { it.dayOfWeek == dayNum }
                        ?: DailyRecordEntity(
                            id = "temp_${habit.id}_$dayNum",
                            weekId = "",
                            date = dateForDay,
                            habitId = habit.id,
                            dayOfWeek = dayNum,
                            status = DayStatus.FUTURE
                        )

                    MobileDayButton(
                        dayName = dayName,
                        dayOfMonth = dayOfMonth,
                        record = record,
                        habit = habit,
                        isToday = isToday,
                        onClick = { onCellClick(record, habit) },
                        onLongClick = { onCellLongClick(record, habit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Mobile-optimized Day Button with touch target >= 44dp height.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MobileDayButton(
    dayName: String,
    dayOfMonth: String,
    record: DailyRecordEntity,
    habit: HabitSnapshot,
    isToday: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isToday) TodayHighlight else TableBorder
    val borderWidth = if (isToday) 1.5.dp else 0.5.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day Label (Mon / Tue...) + Date
        Text(
            text = dayName.take(1),
            fontSize = 11.sp,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isToday) TodayHighlight else TextSecondary
        )
        Text(
            text = dayOfMonth,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isToday) TodayHighlight else TextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Generous touch target (min 44px standard, 46dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .testTag("day_cell_${habit.id}_${record.dayOfWeek}"),
            contentAlignment = Alignment.Center
        ) {
            when (record.status) {
                DayStatus.FUTURE -> {
                    Text(
                        text = "·",
                        fontSize = 16.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
                DayStatus.COMPLETED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(EmeraldSuccessContainer)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.trackingType == HabitType.BOOLEAN) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            val numVal = record.numericValue ?: habit.dailyTarget
                            val displayNum = if (numVal >= 10) numVal.toInt().toString() else String.format("%.1f", numVal).removeSuffix(".0")
                            Text(
                                text = displayNum,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                maxLines = 1
                            )
                        }
                    }
                }
                DayStatus.NOT_COMPLETED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(if (isToday) TodayHighlight.copy(alpha = 0.08f) else CellEmpty)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (record.numericValue != null) {
                            Text(
                                text = "${record.numericValue.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberAttention
                            )
                        }
                    }
                }
                DayStatus.NOT_APPLICABLE -> {
                    Text(
                        text = "—",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

/**
 * Desktop/Tablet Table layout.
 */
@Composable
fun DesktopHabitTable(
    habitProgressList: List<HabitWeekProgress>,
    mondayDate: String,
    todayDate: String,
    onCellClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onCellLongClick: (record: DailyRecordEntity, habit: HabitSnapshot) -> Unit,
    onHabitClick: (habit: HabitSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp)),
        color = CardSurface
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Pinned Left Column (Habit Name & Badge)
            Column(
                modifier = Modifier
                    .width(HabitColWidth)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        color = TableBorder,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HeaderHeight)
                        .background(SurfaceElevated)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "HABIT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                habitProgressList.forEachIndexed { index, progress ->
                    val habit = progress.habit
                    val isAlt = index % 2 == 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(RowHeight)
                            .background(if (isAlt) CellHover else CardSurface)
                            .clickable { onHabitClick(habit) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = habit.icon,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            HabitTypeBadge(type = habit.trackingType)
                        }
                    }
                }
            }

            // Horizontally Scrollable Section (Target + Mon..Sun + Progress)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                // Table Headers
                Row(
                    modifier = Modifier
                        .height(HeaderHeight)
                        .background(SurfaceElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .width(TargetColWidth)
                            .fillMaxHeight()
                            .border(0.5.dp, TableBorder)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    for (dayNum in 1..7) {
                        val dateForDay = DateHelper.getDateForDayOfWeek(mondayDate, dayNum)
                        val isToday = DateHelper.isToday(dateForDay, todayDate)
                        val dayName = DateHelper.getDayShortName(dayNum)
                        val dayOfMonth = try {
                            dateForDay.split("-").last()
                        } catch (e: Exception) {
                            "$dayNum"
                        }

                        val headerBg = if (isToday) TodayHighlight.copy(alpha = 0.2f) else SurfaceElevated
                        val textColor = if (isToday) TodayHighlight else TextPrimary

                        Box(
                            modifier = Modifier
                                .width(DayColWidth)
                                .fillMaxHeight()
                                .background(headerBg)
                                .border(0.5.dp, TableBorder)
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayName.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = dayOfMonth,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isToday) TodayHighlight else TextSecondary
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(ProgressColWidth)
                            .fillMaxHeight()
                            .border(0.5.dp, TableBorder)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Table Rows
                habitProgressList.forEachIndexed { index, progress ->
                    val habit = progress.habit
                    val isAlt = index % 2 == 1

                    Row(
                        modifier = Modifier
                            .height(RowHeight)
                            .background(if (isAlt) CellHover else CardSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(TargetColWidth)
                                .fillMaxHeight()
                                .border(0.5.dp, TableBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            val targetLabel = when (habit.trackingType) {
                                HabitType.LIMIT -> "<${habit.dailyTarget.toInt()}${habit.unit}"
                                HabitType.DURATION -> ">${habit.dailyTarget.toInt()}${habit.unit}"
                                else -> "${habit.weeklyTarget}/wk"
                            }
                            Text(
                                text = targetLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        for (dayNum in 1..7) {
                            val record = progress.dayRecords.find { it.dayOfWeek == dayNum }
                                ?: DailyRecordEntity(
                                    id = "temp_${habit.id}_$dayNum",
                                    weekId = "",
                                    date = DateHelper.getDateForDayOfWeek(mondayDate, dayNum),
                                    habitId = habit.id,
                                    dayOfWeek = dayNum,
                                    status = DayStatus.FUTURE
                                )

                            val isToday = DateHelper.isToday(record.date, todayDate)

                            TableCell(
                                record = record,
                                habit = habit,
                                isToday = isToday,
                                onClick = { onCellClick(record, habit) },
                                onLongClick = { onCellLongClick(record, habit) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(ProgressColWidth)
                                .fillMaxHeight()
                                .border(0.5.dp, TableBorder)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val percent = (progress.successRate * 100).roundToInt()
                                val barColor = when {
                                    percent >= 100 -> EmeraldSuccess
                                    percent >= 60 -> ElectricBlue
                                    percent >= 30 -> AmberAttention
                                    else -> CoralError
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$percent%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                    Text(
                                        text = "${progress.completedDays}/${progress.targetDays}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { progress.successRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = barColor,
                                    trackColor = TableBorder,
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

@Composable
fun TableCell(
    record: DailyRecordEntity,
    habit: HabitSnapshot,
    isToday: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellBorder = if (isToday) TodayHighlight.copy(alpha = 0.8f) else TableBorder
    val borderWidth = if (isToday) 1.5.dp else 0.5.dp

    Box(
        modifier = modifier
            .width(DayColWidth)
            .fillMaxHeight()
            .border(borderWidth, cellBorder)
            .clickable(onClick = onClick)
            .testTag("day_cell_${habit.id}_${record.dayOfWeek}"),
        contentAlignment = Alignment.Center
    ) {
        when (record.status) {
            DayStatus.FUTURE -> {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CellEmpty),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "·",
                        fontSize = 14.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            DayStatus.COMPLETED -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldSuccessContainer)
                        .border(1.dp, EmeraldSuccess, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.trackingType == HabitType.BOOLEAN) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        val numVal = record.numericValue ?: habit.dailyTarget
                        val displayNum = if (numVal >= 10) numVal.toInt().toString() else String.format("%.1f", numVal).removeSuffix(".0")
                        Text(
                            text = displayNum,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            maxLines = 1
                        )
                    }
                }
            }
            DayStatus.NOT_COMPLETED -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CellEmpty)
                        .border(1.dp, CellBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (record.numericValue != null) {
                        Text(
                            text = "${record.numericValue.toInt()}",
                            fontSize = 10.sp,
                            color = AmberAttention
                        )
                    }
                }
            }
            DayStatus.NOT_APPLICABLE -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CellEmpty),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N/A",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
