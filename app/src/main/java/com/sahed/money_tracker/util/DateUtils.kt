package com.sahed.money_tracker.util

import java.util.Calendar

object DateUtils {
    val monthShortNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val monthFullNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getMonthShortName(month: Int): String {
        return if (month in 1..12) monthShortNames[month - 1] else "M$month"
    }

    fun getMonthFullName(month: Int): String {
        return if (month in 1..12) monthFullNames[month - 1] else "Month $month"
    }
}
