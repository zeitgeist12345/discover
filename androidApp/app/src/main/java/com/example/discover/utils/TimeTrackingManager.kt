package com.example.discover.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import androidx.core.content.edit

class TimeTrackingManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "time_tracking_prefs"
        private const val KEY_TOTAL_TIME = "total_time_ms"
        private const val KEY_LAST_START_TIME = "last_start_time"
        private const val KEY_DAILY_TIME = "daily_time_"
        private const val KEY_WEEKLY_TIME = "weekly_time_"
        private const val KEY_MONTHLY_TIME = "monthly_time_"
        private const val KEY_YEARLY_TIME = "yearly_time_"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun startSession() {
        val currentTime = System.currentTimeMillis()
        prefs.edit { putLong(KEY_LAST_START_TIME, currentTime) }
    }

    fun endSession() {
        val startTime = prefs.getLong(KEY_LAST_START_TIME, 0L)
        if (startTime > 0) {
            val sessionDuration = System.currentTimeMillis() - startTime

            updateAllTimeStats(sessionDuration)

            // Reset start time
            prefs.edit { putLong(KEY_LAST_START_TIME, 0L) }
        }
    }

    private fun updateAllTimeStats(duration: Long) {
        val calendar = Calendar.getInstance()

        // Update total time
        val totalTime = prefs.getLong(KEY_TOTAL_TIME, 0L) + duration
        prefs.edit { putLong(KEY_TOTAL_TIME, totalTime) }

        // Update daily time
        val dayKey =
            "${KEY_DAILY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.DAY_OF_YEAR)}"
        val dailyTime = prefs.getLong(dayKey, 0L) + duration
        prefs.edit { putLong(dayKey, dailyTime) }

        // Update weekly time
        val weekKey =
            "${KEY_WEEKLY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.WEEK_OF_YEAR)}"
        val weeklyTime = prefs.getLong(weekKey, 0L) + duration
        prefs.edit { putLong(weekKey, weeklyTime) }

        // Update monthly time
        val monthKey =
            "${KEY_MONTHLY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)}"
        val monthlyTime = prefs.getLong(monthKey, 0L) + duration
        prefs.edit { putLong(monthKey, monthlyTime) }

        // Update yearly time
        val yearKey = "${KEY_YEARLY_TIME}${calendar.get(Calendar.YEAR)}"
        val yearlyTime = prefs.getLong(yearKey, 0L) + duration
        prefs.edit { putLong(yearKey, yearlyTime) }
    }

    fun getTimeStats(): TimeStats {
        val calendar = Calendar.getInstance()

        // Current period keys
        val currentDayKey =
            "${KEY_DAILY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.DAY_OF_YEAR)}"

        // Get yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Move to yesterday
        val yesterdayDayKey =
            "${KEY_DAILY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.DAY_OF_YEAR)}"
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Reset back to today

        val currentWeekKey =
            "${KEY_WEEKLY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.WEEK_OF_YEAR)}"
        val currentMonthKey =
            "${KEY_MONTHLY_TIME}${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)}"
        val currentYearKey = "${KEY_YEARLY_TIME}${calendar.get(Calendar.YEAR)}"

        return TimeStats(
            daily = prefs.getLong(currentDayKey, 0L),
            yesterday = prefs.getLong(yesterdayDayKey, 0L),
            weekly = prefs.getLong(currentWeekKey, 0L),
            monthly = prefs.getLong(currentMonthKey, 0L),
            yearly = prefs.getLong(currentYearKey, 0L),
            total = prefs.getLong(KEY_TOTAL_TIME, 0L),
        )
    }

}

data class TimeStats(
    val daily: Long,
    val yesterday: Long,
    val weekly: Long,
    val monthly: Long,
    val yearly: Long,
    val total: Long,
)