package com.example.domain.calculator

import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.data.model.HabitWeekProgress
import com.example.data.model.NextWeekSuggestion
import com.example.data.model.PatternInsight
import com.example.data.model.WeekComparison
import com.example.data.model.WeeklyInsightSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class MonthlyStats(
    val monthKey: String,
    val monthDisplayName: String,
    val overallPercentage: Int,
    val previousMonthPercentage: Int?,
    val deltaFromPrevious: Int?,
    val bestHabit: Pair<HabitSnapshot, Int>?,
    val weakestHabit: Pair<HabitSnapshot, Int>?,
    val bestDay: Pair<String, Int>?,
    val weakestDay: Pair<String, Int>?,
    val totalSuccessfulHabitDays: Int,
    val totalPossibleHabitDays: Int,
    val averageDailyCompleted: Float,
    val habitBreakdown: List<Pair<HabitSnapshot, Int>>
)

data class MultiPeriodStats(
    val title: String,
    val averageCompletion: Int,
    val trendDelta: Int,
    val bestHabit: Pair<HabitSnapshot, Int>?,
    val weakestHabit: Pair<HabitSnapshot, Int>?,
    val totalSuccessfulDays: Int,
    val consistencyScore: Int,
    val habitRankings: List<Pair<HabitSnapshot, Int>>
)

data class YearlyStats(
    val year: String,
    val overallPercentage: Int,
    val bestMonth: Pair<String, Int>?,
    val weakestMonth: Pair<String, Int>?,
    val monthlyTrend: List<Pair<String, Int>>,
    val habitConsistency: List<Pair<HabitSnapshot, Int>>
)

object HabitCalculator {

    /**
     * Determine if a daily record counts as a success based on habit tracking type.
     */
    fun isDaySuccess(
        trackingType: HabitType,
        dailyTarget: Float,
        record: DailyRecordEntity?
    ): Boolean {
        if (record == null) return false
        if (record.status == DayStatus.FUTURE || record.status == DayStatus.NOT_APPLICABLE) return false

        return when (trackingType) {
            HabitType.BOOLEAN -> record.isCompleted
            HabitType.NUMERIC, HabitType.DURATION -> {
                val value = record.numericValue ?: (if (record.isCompleted) dailyTarget else 0f)
                value >= dailyTarget
            }
            HabitType.LIMIT -> {
                // For limits (e.g. mobile < 2 hr), staying at or below target is success
                if (record.numericValue != null) {
                    record.numericValue <= dailyTarget
                } else {
                    record.isCompleted
                }
            }
        }
    }

    /**
     * Calculate progress for a specific habit in a week.
     */
    fun calculateHabitProgress(
        habit: HabitSnapshot,
        recordsForHabit: List<DailyRecordEntity>,
        todayDate: String
    ): HabitWeekProgress {
        var completedDays = 0

        val sortedRecords = (1..7).map { dayNum ->
            val existing = recordsForHabit.find { it.dayOfWeek == dayNum }
            if (existing != null) {
                if (isDaySuccess(habit.trackingType, habit.dailyTarget, existing)) {
                    completedDays++
                }
                existing
            } else {
                DailyRecordEntity(
                    id = "temp_${habit.id}_$dayNum",
                    weekId = "",
                    date = "",
                    habitId = habit.id,
                    dayOfWeek = dayNum,
                    status = DayStatus.FUTURE
                )
            }
        }

        val target = habit.weeklyTarget.coerceAtLeast(1)
        val successRate = (completedDays.toFloat() / target).coerceIn(0f, 1f)
        val isAdherent = completedDays >= target

        return HabitWeekProgress(
            habit = habit,
            targetDays = target,
            completedDays = completedDays,
            successRate = successRate,
            isAdherent = isAdherent,
            dayRecords = sortedRecords
        )
    }

    /**
     * Calculate overall weekly completion percentage.
     * Future days DO NOT hurt current week's percentage.
     */
    fun calculateWeeklyProgress(
        habitProgressList: List<HabitWeekProgress>,
        allRecords: List<DailyRecordEntity>,
        todayDate: String
    ): Int {
        if (habitProgressList.isEmpty()) return 0

        // Check elapsed days in week (days <= today)
        var totalElapsedDays = 0
        var totalElapsedSuccesses = 0

        for (progress in habitProgressList) {
            for (rec in progress.dayRecords) {
                if (rec.status != DayStatus.FUTURE && rec.status != DayStatus.NOT_APPLICABLE) {
                    totalElapsedDays++
                    if (isDaySuccess(progress.habit.trackingType, progress.habit.dailyTarget, rec)) {
                        totalElapsedSuccesses++
                    }
                }
            }
        }

        return if (totalElapsedDays > 0) {
            ((totalElapsedSuccesses.toFloat() / totalElapsedDays) * 100f).roundToInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    /**
     * Compare current week with previous week.
     */
    fun compareWeeks(
        currentScore: Int,
        previousScore: Int,
        currentHabits: List<HabitWeekProgress>,
        previousHabits: List<HabitWeekProgress>
    ): WeekComparison {
        val scoreDelta = currentScore - previousScore
        val improved = mutableListOf<String>()
        val declined = mutableListOf<String>()
        val stable = mutableListOf<String>()

        val prevMap = previousHabits.associateBy { it.habit.id }

        for (curr in currentHabits) {
            val prev = prevMap[curr.habit.id]
            if (prev != null) {
                val currPercent = (curr.successRate * 100).roundToInt()
                val prevPercent = (prev.successRate * 100).roundToInt()
                val diff = currPercent - prevPercent
                if (diff >= 5) {
                    improved.add(curr.habit.name)
                } else if (diff <= -5) {
                    declined.add(curr.habit.name)
                } else {
                    stable.add(curr.habit.name)
                }
            }
        }

        return WeekComparison(
            currentScore = currentScore,
            previousScore = previousScore,
            scoreDelta = scoreDelta,
            habitsImproved = improved,
            habitsDeclined = declined,
            habitsStable = stable
        )
    }

    /**
     * Calculate day-of-week performance across a list of weekly records.
     * Returns a map of dayOfWeek (1=Mon..7=Sun) to success percentage (0..100).
     */
    fun calculateDayPerformance(
        records: List<DailyRecordEntity>,
        habitsMap: Map<String, HabitSnapshot>
    ): Map<Int, Int> {
        val dayCounts = mutableMapOf<Int, Int>()
        val daySuccesses = mutableMapOf<Int, Int>()

        for (rec in records) {
            if (rec.status == DayStatus.FUTURE || rec.status == DayStatus.NOT_APPLICABLE) continue
            val habit = habitsMap[rec.habitId] ?: continue
            val dow = rec.dayOfWeek
            dayCounts[dow] = (dayCounts[dow] ?: 0) + 1
            if (isDaySuccess(habit.trackingType, habit.dailyTarget, rec)) {
                daySuccesses[dow] = (daySuccesses[dow] ?: 0) + 1
            }
        }

        val result = mutableMapOf<Int, Int>()
        for (i in 1..7) {
            val total = dayCounts[i] ?: 0
            val success = daySuccesses[i] ?: 0
            result[i] = if (total > 0) ((success.toFloat() / total) * 100).roundToInt() else 0
        }
        return result
    }

    /**
     * Calculate current streak and longest streak of consecutive days a habit was completed.
     */
    fun calculateHabitStreaks(
        habitId: String,
        habit: HabitSnapshot,
        chronologicalRecords: List<DailyRecordEntity>
    ): Pair<Int, Int> {
        // Records sorted from oldest to newest
        val validRecords = chronologicalRecords
            .filter { it.habitId == habitId && it.status != DayStatus.FUTURE && it.status != DayStatus.NOT_APPLICABLE }
            .sortedBy { it.date }

        if (validRecords.isEmpty()) return Pair(0, 0)

        var longestStreak = 0
        var currentStreak = 0
        var runningStreak = 0

        for (rec in validRecords) {
            val success = isDaySuccess(habit.trackingType, habit.dailyTarget, rec)
            if (success) {
                runningStreak++
                if (runningStreak > longestStreak) {
                    longestStreak = runningStreak
                }
            } else {
                runningStreak = 0
            }
        }

        // Current streak counting backwards from the most recent day
        for (rec in validRecords.reversed()) {
            val success = isDaySuccess(habit.trackingType, habit.dailyTarget, rec)
            if (success) {
                currentStreak++
            } else {
                break
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Generate weekly insights and summary.
     */
    fun generateWeeklyInsights(
        weekId: String,
        dateRange: String,
        habitProgressList: List<HabitWeekProgress>,
        allWeekRecords: List<DailyRecordEntity>,
        previousWeekProgress: List<HabitWeekProgress>? = null,
        todayDate: String
    ): WeeklyInsightSummary {
        val overall = calculateWeeklyProgress(habitProgressList, allWeekRecords, todayDate)
        val adherentCount = habitProgressList.count { it.isAdherent }
        val totalHabits = habitProgressList.size

        // Find best and weakest habit
        val activeCompleted = habitProgressList.sortedByDescending { it.successRate }
        val bestHabit = activeCompleted.firstOrNull()?.let { "${it.habit.icon} ${it.habit.name} (${(it.successRate * 100).roundToInt()}%)" }
        val weakestHabit = activeCompleted.lastOrNull()?.let { "${it.habit.icon} ${it.habit.name} (${(it.successRate * 100).roundToInt()}%)" }

        // Day performance for this week
        val habitsMap = habitProgressList.associate { it.habit.id to it.habit }
        val dayPerf = calculateDayPerformance(allWeekRecords, habitsMap)

        val validDays = dayPerf.filter { (dow, _) ->
            allWeekRecords.any { it.dayOfWeek == dow && it.status != DayStatus.FUTURE }
        }

        val bestDayEntry = validDays.maxByOrNull { it.value }
        val weakestDayEntry = validDays.minByOrNull { it.value }

        val bestDay = bestDayEntry?.let { "${DateHelper.getDayShortName(it.key)} (${it.value}%)" }
        val weakestDay = weakestDayEntry?.let { "${DateHelper.getDayShortName(it.key)} (${it.value}%)" }

        var totalSuccess = 0
        var totalMissed = 0
        for (prog in habitProgressList) {
            for (rec in prog.dayRecords) {
                if (rec.status != DayStatus.FUTURE && rec.status != DayStatus.NOT_APPLICABLE) {
                    if (isDaySuccess(prog.habit.trackingType, prog.habit.dailyTarget, rec)) {
                        totalSuccess++
                    } else {
                        totalMissed++
                    }
                }
            }
        }

        val comparison = if (previousWeekProgress != null && previousWeekProgress.isNotEmpty()) {
            val prevScore = (previousWeekProgress.map { it.successRate }.average() * 100).roundToInt()
            compareWeeks(overall, prevScore, habitProgressList, previousWeekProgress)
        } else null

        return WeeklyInsightSummary(
            weekId = weekId,
            dateRange = dateRange,
            overallPercentage = overall,
            targetAdherenceCount = adherentCount,
            totalHabitsCount = totalHabits,
            bestHabit = bestHabit,
            weakestHabit = weakestHabit,
            bestDay = bestDay,
            weakestDay = weakestDay,
            totalSuccessfulHabitDays = totalSuccess,
            missedHabitDays = totalMissed,
            comparison = comparison,
            habitProgressList = habitProgressList
        )
    }

    /**
     * Generate Next Week Planning recommendations based on actual historical data.
     * System only recommends; user decides whether to accept.
     */
    fun generateNextWeekRecommendations(
        currentHabits: List<HabitWeekProgress>,
        previousHabits: List<HabitWeekProgress>?
    ): List<NextWeekSuggestion> {
        val prevMap = previousHabits?.associateBy { it.habit.id } ?: emptyMap()

        return currentHabits.map { curr ->
            val prev = prevMap[curr.habit.id]
            val currPercent = (curr.successRate * 100).roundToInt()
            val prevPercent = prev?.let { (it.successRate * 100).roundToInt() }

            val trend = when {
                prevPercent == null -> "Stable"
                currPercent > prevPercent + 5 -> "Improved"
                currPercent < prevPercent - 5 -> "Declined"
                else -> "Stable"
            }

            val (action, reason) = when {
                curr.completedDays >= curr.targetDays && curr.targetDays < 7 && (prev?.completedDays ?: 0) >= (prev?.targetDays ?: 0) -> {
                    Pair("Increase target", "You met or exceeded your target of ${curr.targetDays} days consistently.")
                }
                currPercent >= 90 && curr.targetDays == 7 -> {
                    Pair("Keep target", "Perfect mastery! Maintain this consistency next week.")
                }
                currPercent in 65..89 -> {
                    Pair("Keep target", "Solid progress. Staying at ${curr.targetDays} days reinforces your routine.")
                }
                currPercent in 35..64 && curr.targetDays > 3 -> {
                    Pair("Reduce target", "You completed ${curr.completedDays} of ${curr.targetDays} days. Lowering target to ${curr.completedDays.coerceAtLeast(3)} can build sustainable momentum.")
                }
                currPercent < 35 -> {
                    Pair("Review habit", "Completion was ${currPercent}%. Consider adjusting the time of day, unit, or environment.")
                }
                else -> {
                    Pair("Keep target", "Maintain current pace.")
                }
            }

            NextWeekSuggestion(
                habitId = curr.habit.id,
                habitName = curr.habit.name,
                icon = curr.habit.icon,
                previousTarget = curr.targetDays,
                previousCompletion = curr.completedDays,
                trend = trend,
                suggestedAction = action,
                rationalReason = reason
            )
        }
    }

    /**
     * Detect Personal Patterns strictly supported by stored data.
     * Never invents cause; reports empirical correlations.
     */
    fun detectPersonalPatterns(
        allRecords: List<DailyRecordEntity>,
        habits: List<HabitSnapshot>
    ): List<PatternInsight> {
        val insights = mutableListOf<PatternInsight>()
        val habitsMap = habits.associateBy { it.id }

        // Day performance pattern
        val dayPerf = calculateDayPerformance(allRecords, habitsMap)
        val validDayPerfs = dayPerf.filter { it.value > 0 }
        if (validDayPerfs.size >= 5) {
            val minDay = validDayPerfs.minByOrNull { it.value }
            val maxDay = validDayPerfs.maxByOrNull { it.value }

            if (minDay != null && maxDay != null && maxDay.value - minDay.value >= 15) {
                insights.add(
                    PatternInsight(
                        title = "Strongest vs Lowest Day",
                        description = "${DateHelper.getDayShortName(maxDay.key)}s have your highest average completion at ${maxDay.value}%, while ${DateHelper.getDayShortName(minDay.key)}s drop to ${minDay.value}%.",
                        type = "DAY_PERFORMANCE",
                        isPositive = true
                    )
                )
            }
        }

        // Habit correlation: e.g. "When Habit A was completed, Habit B was also completed on X of Y days"
        // Check pairwise correlations for habits with at least 10 logged days
        val habitDatesCompleted = mutableMapOf<String, Set<String>>()
        for (habit in habits) {
            val completedDates = allRecords
                .filter { it.habitId == habit.id && isDaySuccess(habit.trackingType, habit.dailyTarget, it) }
                .map { it.date }
                .toSet()
            if (completedDates.size >= 5) {
                habitDatesCompleted[habit.id] = completedDates
            }
        }

        var foundCorrelation = false
        val habitList = habits.filter { habitDatesCompleted.containsKey(it.id) }
        for (i in 0 until habitList.size) {
            if (foundCorrelation) break
            for (j in i + 1 until habitList.size) {
                val h1 = habitList[i]
                val h2 = habitList[j]
                val d1 = habitDatesCompleted[h1.id] ?: emptySet()
                val d2 = habitDatesCompleted[h2.id] ?: emptySet()
                val common = d1.intersect(d2)

                if (d1.size >= 6 && common.size.toFloat() / d1.size >= 0.75f) {
                    insights.add(
                        PatternInsight(
                            title = "Habit Pair Correlation",
                            description = "These two habits (${h1.icon} ${h1.name} and ${h2.icon} ${h2.name}) were completed together on ${common.size} of ${d1.size} days.",
                            type = "HABIT_CORRELATION",
                            isPositive = true
                        )
                    )
                    foundCorrelation = true
                    break
                }
            }
        }

        return insights
    }

    /**
     * Aggregates stats for a specific month (e.g. "2026-09").
     */
    fun calculateMonthlyStats(
        targetYearMonth: String,
        allRecords: List<DailyRecordEntity>,
        habits: List<HabitSnapshot>,
        todayDate: String
    ): MonthlyStats {
        val monthRecords = allRecords.filter { it.date.startsWith(targetYearMonth) }
        val pastOrTodayRecords = monthRecords.filter { !DateHelper.isFutureDay(it.date, todayDate) }

        val prevMonthKey = DateHelper.getPreviousMonthKey(targetYearMonth)
        val prevRecords = allRecords.filter { it.date.startsWith(prevMonthKey) && !DateHelper.isFutureDay(it.date, todayDate) }

        var totalMonthSuccess = 0
        var totalMonthPossible = 0

        val habitScores = habits.map { habit ->
            val hRecords = pastOrTodayRecords.filter { it.habitId == habit.id }
            val successes = hRecords.count { isDaySuccess(habit.trackingType, habit.dailyTarget, it) }
            totalMonthSuccess += successes
            totalMonthPossible += hRecords.size
            val rate = if (hRecords.isNotEmpty()) (successes.toFloat() / hRecords.size * 100).roundToInt() else 0
            Pair(habit, rate)
        }.sortedByDescending { it.second }

        val bestHabit = habitScores.firstOrNull()
        val weakestHabit = habitScores.lastOrNull()

        val overallRate = if (totalMonthPossible > 0) (totalMonthSuccess.toFloat() / totalMonthPossible * 100).roundToInt() else 0

        val prevOverallRate = if (prevRecords.isNotEmpty()) {
            var pSuccess = 0
            habits.forEach { habit ->
                val pRecs = prevRecords.filter { it.habitId == habit.id }
                pSuccess += pRecs.count { isDaySuccess(habit.trackingType, habit.dailyTarget, it) }
            }
            if (prevRecords.isNotEmpty()) (pSuccess.toFloat() / prevRecords.size * 100).roundToInt() else null
        } else null

        val delta = prevOverallRate?.let { overallRate - it }

        val recordsByDate = pastOrTodayRecords.groupBy { it.date }
        val dayRates = recordsByDate.mapNotNull { (date, recs) ->
            if (recs.isEmpty()) null
            else {
                var s = 0
                recs.forEach { r ->
                    val habit = habits.find { it.id == r.habitId }
                    if (habit != null && isDaySuccess(habit.trackingType, habit.dailyTarget, r)) {
                        s++
                    }
                }
                val rate = (s.toFloat() / recs.size * 100).roundToInt()
                val display = DateHelper.formatDayDisplay(date)
                Triple(display, rate, s)
            }
        }.sortedByDescending { it.second }

        val bestDay = dayRates.firstOrNull()?.let { Pair(it.first, it.second) }
        val weakestDay = dayRates.lastOrNull()?.let { Pair(it.first, it.second) }

        val uniqueDaysCount = recordsByDate.size.coerceAtLeast(1)
        val avgDailyCompleted = if (uniqueDaysCount > 0) totalMonthSuccess.toFloat() / uniqueDaysCount else 0f

        return MonthlyStats(
            monthKey = targetYearMonth,
            monthDisplayName = DateHelper.getMonthDisplay(targetYearMonth),
            overallPercentage = overallRate,
            previousMonthPercentage = prevOverallRate,
            deltaFromPrevious = delta,
            bestHabit = bestHabit,
            weakestHabit = weakestHabit,
            bestDay = bestDay,
            weakestDay = weakestDay,
            totalSuccessfulHabitDays = totalMonthSuccess,
            totalPossibleHabitDays = totalMonthPossible,
            averageDailyCompleted = avgDailyCompleted,
            habitBreakdown = habitScores
        )
    }

    /**
     * Aggregates stats for 3 Months or 6 Months.
     */
    fun calculateMultiPeriodStats(
        monthsBack: Int,
        allRecords: List<DailyRecordEntity>,
        habits: List<HabitSnapshot>,
        todayDate: String
    ): MultiPeriodStats {
        val title = if (monthsBack == 3) "Past 3 Months" else "Past 6 Months"
        val cal = Calendar.getInstance()
        val today = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(todayDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        cal.time = today
        cal.add(Calendar.MONTH, -monthsBack)
        val cutoffStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        val periodRecords = allRecords.filter { it.date >= cutoffStr && !DateHelper.isFutureDay(it.date, todayDate) }
        val totalPossible = periodRecords.size
        var totalSuccess = 0

        val habitRankings = habits.map { habit ->
            val hRecs = periodRecords.filter { it.habitId == habit.id }
            val s = hRecs.count { isDaySuccess(habit.trackingType, habit.dailyTarget, it) }
            totalSuccess += s
            val pct = if (hRecs.isNotEmpty()) (s.toFloat() / hRecs.size * 100).roundToInt() else 0
            Pair(habit, pct)
        }.sortedByDescending { it.second }

        val avg = if (totalPossible > 0) (totalSuccess.toFloat() / totalPossible * 100).roundToInt() else 0

        val sortedDates = periodRecords.map { it.date }.distinct().sorted()
        val half = sortedDates.size / 2
        val trendDelta = if (half > 0) {
            val firstHalfDates = sortedDates.take(half).toSet()
            val secondHalfDates = sortedDates.drop(half).toSet()

            val firstHalfRecs = periodRecords.filter { firstHalfDates.contains(it.date) }
            val secondHalfRecs = periodRecords.filter { secondHalfDates.contains(it.date) }

            val firstSuccess = firstHalfRecs.count { r ->
                val h = habits.find { it.id == r.habitId }
                h != null && isDaySuccess(h.trackingType, h.dailyTarget, r)
            }
            val secondSuccess = secondHalfRecs.count { r ->
                val h = habits.find { it.id == r.habitId }
                h != null && isDaySuccess(h.trackingType, h.dailyTarget, r)
            }

            val r1 = if (firstHalfRecs.isNotEmpty()) (firstSuccess.toFloat() / firstHalfRecs.size * 100).roundToInt() else avg
            val r2 = if (secondHalfRecs.isNotEmpty()) (secondSuccess.toFloat() / secondHalfRecs.size * 100).roundToInt() else avg
            r2 - r1
        } else 0

        return MultiPeriodStats(
            title = title,
            averageCompletion = avg,
            trendDelta = trendDelta,
            bestHabit = habitRankings.firstOrNull(),
            weakestHabit = habitRankings.lastOrNull(),
            totalSuccessfulDays = totalSuccess,
            consistencyScore = (avg * 0.95f).roundToInt(),
            habitRankings = habitRankings
        )
    }

    /**
     * Aggregates stats for a given year (e.g. "2026").
     */
    fun calculateYearlyStats(
        targetYear: String,
        allRecords: List<DailyRecordEntity>,
        habits: List<HabitSnapshot>,
        todayDate: String
    ): YearlyStats {
        val yearRecords = allRecords.filter { it.date.startsWith(targetYear) && !DateHelper.isFutureDay(it.date, todayDate) }
        var totalSuccess = 0
        val totalPossible = yearRecords.size

        val habitsRanked = habits.map { habit ->
            val hRecs = yearRecords.filter { it.habitId == habit.id }
            val s = hRecs.count { isDaySuccess(habit.trackingType, habit.dailyTarget, it) }
            totalSuccess += s
            val pct = if (hRecs.isNotEmpty()) (s.toFloat() / hRecs.size * 100).roundToInt() else 0
            Pair(habit, pct)
        }.sortedByDescending { it.second }

        val overall = if (totalPossible > 0) (totalSuccess.toFloat() / totalPossible * 100).roundToInt() else 0

        val monthKeys = yearRecords.map { it.date.substring(0, 7) }.distinct().sorted()
        val monthlyScores = monthKeys.mapNotNull { mKey ->
            val mRecs = yearRecords.filter { it.date.startsWith(mKey) }
            if (mRecs.isEmpty()) null
            else {
                var s = 0
                mRecs.forEach { r ->
                    val h = habits.find { it.id == r.habitId }
                    if (h != null && isDaySuccess(h.trackingType, h.dailyTarget, r)) s++
                }
                val pct = (s.toFloat() / mRecs.size * 100).roundToInt()
                val monthShort = try {
                    val d = SimpleDateFormat("yyyy-MM", Locale.US).parse(mKey)
                    if (d != null) SimpleDateFormat("MMM", Locale.US).format(d) else mKey
                } catch (e: Exception) {
                    mKey
                }
                Pair(monthShort, pct)
            }
        }

        val bestM = monthlyScores.maxByOrNull { it.second }
        val worstM = monthlyScores.minByOrNull { it.second }

        return YearlyStats(
            year = targetYear,
            overallPercentage = overall,
            bestMonth = bestM,
            weakestMonth = worstM,
            monthlyTrend = monthlyScores,
            habitConsistency = habitsRanked
        )
    }
}
