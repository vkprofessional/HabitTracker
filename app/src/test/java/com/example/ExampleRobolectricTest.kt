package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DailyRecordEntity
import com.example.data.model.DayStatus
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.domain.calculator.HabitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Habit Tracker", appName)
  }

  @Test
  fun `test habit progress calculation with target`() {
    val habit = HabitSnapshot(
      id = "h1",
      name = "Exercise",
      icon = "🏋️",
      category = "Health",
      trackingType = HabitType.BOOLEAN,
      weeklyTarget = 5,
      dailyTarget = 1f,
      unit = ""
    )

    val records = listOf(
      DailyRecordEntity("r1", "w1", "2026-09-21", "h1", 1, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r2", "w1", "2026-09-22", "h1", 2, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r3", "w1", "2026-09-23", "h1", 3, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r4", "w1", "2026-09-24", "h1", 4, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r5", "w1", "2026-09-25", "h1", 5, isCompleted = false, status = DayStatus.NOT_COMPLETED),
      DailyRecordEntity("r6", "w1", "2026-09-26", "h1", 6, isCompleted = false, status = DayStatus.FUTURE),
      DailyRecordEntity("r7", "w1", "2026-09-27", "h1", 7, isCompleted = false, status = DayStatus.FUTURE)
    )

    val progress = HabitCalculator.calculateHabitProgress(habit, records, "2026-09-25")
    assertEquals(4, progress.completedDays)
    assertEquals(5, progress.targetDays)
    assertEquals(0.8f, progress.successRate, 0.01f)
  }

  @Test
  fun `test monthly stats aggregation derived from records`() {
    val habit1 = HabitSnapshot(
      id = "h1",
      name = "Workout",
      icon = "🏃",
      category = "Fitness",
      trackingType = HabitType.BOOLEAN,
      weeklyTarget = 5,
      dailyTarget = 1f,
      unit = ""
    )

    val habit2 = HabitSnapshot(
      id = "h2",
      name = "Read",
      icon = "📚",
      category = "Mind",
      trackingType = HabitType.BOOLEAN,
      weeklyTarget = 7,
      dailyTarget = 1f,
      unit = ""
    )

    val records = listOf(
      DailyRecordEntity("r1", "w1", "2026-09-01", "h1", 2, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r2", "w1", "2026-09-01", "h2", 2, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r3", "w1", "2026-09-02", "h1", 3, isCompleted = true, status = DayStatus.COMPLETED),
      DailyRecordEntity("r4", "w1", "2026-09-02", "h2", 3, isCompleted = false, status = DayStatus.NOT_COMPLETED),
      DailyRecordEntity("r5", "w1", "2026-09-03", "h1", 4, isCompleted = false, status = DayStatus.NOT_COMPLETED),
      DailyRecordEntity("r6", "w1", "2026-09-03", "h2", 4, isCompleted = false, status = DayStatus.NOT_COMPLETED)
    )

    val monthlyStats = HabitCalculator.calculateMonthlyStats("2026-09", records, listOf(habit1, habit2), "2026-09-03")
    assertEquals("2026-09", monthlyStats.monthKey)
    assertEquals(3, monthlyStats.totalSuccessfulHabitDays)
    assertEquals(6, monthlyStats.totalPossibleHabitDays)
    assertEquals(50, monthlyStats.overallPercentage)
    assertEquals("Workout", monthlyStats.bestHabit?.first?.name)
  }
}

