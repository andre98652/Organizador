package com.example.organizador.util

import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtils {
    /**
     * Calculates the number of days remaining until the given due date.
     * The input [dueDate] is expected to be a UTC timestamp at midnight.
     * The calculation compares this against "today" normalized to UTC midnight.
     */
    fun getDaysRemaining(dueDate: Long): Int {
        // 1. Get current local date components
        val localCal = Calendar.getInstance()
        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH)
        val day = localCal.get(Calendar.DAY_OF_MONTH)

        // 2. Create UTC timestamp for this local date (normalized to midnight UTC)
        val todayUtcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        todayUtcCal.clear()
        todayUtcCal.set(year, month, day)
        val todayUtcMillis = todayUtcCal.timeInMillis

        // 3. Calculate difference
        val diff = dueDate - todayUtcMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
}
