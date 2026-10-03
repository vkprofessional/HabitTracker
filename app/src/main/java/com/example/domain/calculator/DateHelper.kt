package com.example.domain.calculator

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.US)
    private val fullDisplayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    @Synchronized
    fun getTodayDateString(): String {
        return isoDateFormat.format(Date())
    }

    @Synchronized
    fun getMondayOfWeek(dateString: String): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val date = isoDateFormat.parse(dateString) ?: Date()
        cal.time = date
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return isoDateFormat.format(cal.time)
    }

    @Synchronized
    fun getSundayOfWeek(mondayDateString: String): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val date = isoDateFormat.parse(mondayDateString) ?: Date()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, 6)
        return isoDateFormat.format(cal.time)
    }

    @Synchronized
    fun getWeekId(mondayDateString: String): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.minimalDaysInFirstWeek = 4
        val date = isoDateFormat.parse(mondayDateString) ?: Date()
        cal.time = date
        val year = cal.get(Calendar.YEAR)
        val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, weekOfYear)
    }

    @Synchronized
    fun getWeekDisplayRange(mondayDateString: String): String {
        return try {
            val monDate = isoDateFormat.parse(mondayDateString) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = monDate
            val monMonth = SimpleDateFormat("MMM", Locale.US).format(monDate)
            val monDay = SimpleDateFormat("d", Locale.US).format(monDate)
            val monYear = SimpleDateFormat("yyyy", Locale.US).format(monDate)

            cal.add(Calendar.DAY_OF_YEAR, 6)
            val sunDate = cal.time
            val sunMonth = SimpleDateFormat("MMM", Locale.US).format(sunDate)
            val sunDay = SimpleDateFormat("d", Locale.US).format(sunDate)
            val sunYear = SimpleDateFormat("yyyy", Locale.US).format(sunDate)

            if (monMonth == sunMonth && monYear == sunYear) {
                "Week of $monMonth $monDay–$sunDay, $monYear"
            } else if (monYear == sunYear) {
                "Week of $monMonth $monDay – $sunMonth $sunDay, $monYear"
            } else {
                "Week of $monMonth $monDay, $monYear – $sunMonth $sunDay, $sunYear"
            }
        } catch (e: Exception) {
            "Week of $mondayDateString"
        }
    }

    @Synchronized
    fun getDateForDayOfWeek(mondayDateString: String, dayOfWeek: Int): String {
        val cal = Calendar.getInstance()
        val date = isoDateFormat.parse(mondayDateString) ?: Date()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, dayOfWeek - 1)
        return isoDateFormat.format(cal.time)
    }

    @Synchronized
    fun getDayOfWeek(dateString: String): Int {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val date = isoDateFormat.parse(dateString) ?: Date()
        cal.time = date
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY is 2, SUNDAY is 1
        return when (dow) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getDayShortName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> "Day"
        }
    }

    @Synchronized
    fun isFutureDay(targetDateString: String, todayDateString: String): Boolean {
        return try {
            val target = isoDateFormat.parse(targetDateString)
            val today = isoDateFormat.parse(todayDateString)
            if (target != null && today != null) {
                target.after(today)
            } else false
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun isToday(targetDateString: String, todayDateString: String): Boolean {
        return targetDateString == todayDateString
    }

    @Synchronized
    fun isPastDay(targetDateString: String, todayDateString: String): Boolean {
        return try {
            val target = isoDateFormat.parse(targetDateString)
            val today = isoDateFormat.parse(todayDateString)
            if (target != null && today != null) {
                target.before(today)
            } else false
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun getPreviousMonday(mondayDateString: String): String {
        val cal = Calendar.getInstance()
        val date = isoDateFormat.parse(mondayDateString) ?: Date()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, -7)
        return isoDateFormat.format(cal.time)
    }

    @Synchronized
    fun getNextMonday(mondayDateString: String): String {
        val cal = Calendar.getInstance()
        val date = isoDateFormat.parse(mondayDateString) ?: Date()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, 7)
        return isoDateFormat.format(cal.time)
    }

    @Synchronized
    fun getMonthDisplay(yearMonth: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM", Locale.US)
            val outFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
            val d = format.parse(yearMonth)
            if (d != null) outFormat.format(d) else yearMonth
        } catch (e: Exception) {
            yearMonth
        }
    }

    @Synchronized
    fun formatDayDisplay(dateString: String): String {
        return try {
            val d = isoDateFormat.parse(dateString)
            if (d != null) SimpleDateFormat("MMMM d", Locale.US).format(d) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    @Synchronized
    fun getPreviousMonthKey(yearMonth: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM", Locale.US)
            val d = format.parse(yearMonth) ?: return yearMonth
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MONTH, -1)
            format.format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }

    @Synchronized
    fun getNextMonthKey(yearMonth: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM", Locale.US)
            val d = format.parse(yearMonth) ?: return yearMonth
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MONTH, 1)
            format.format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }
}
