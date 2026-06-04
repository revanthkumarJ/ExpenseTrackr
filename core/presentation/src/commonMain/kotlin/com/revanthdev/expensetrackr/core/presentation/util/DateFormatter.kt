package com.revanthdev.expensetrackr.core.presentation.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

private val MONTHS = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

fun LocalDateTime.toDisplayDate(): String {
    val tz = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> "${date.dayOfMonth} ${MONTHS[date.monthNumber - 1]} ${date.year}"
    }
}

fun LocalDateTime.toDisplayTime(): String {
    val h = hour
    val m = minute.toString().padStart(2, '0')
    val amPm = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "$h12:$m $amPm"
}

fun LocalDateTime.toMonthYear(): String =
    "${MONTHS[date.monthNumber - 1]} ${date.year}"

fun Double.toCurrencyString(): String {
    val intPart = toLong()
    val decPart = ((this - intPart) * 100).toLong()
    return "₹$intPart.${decPart.toString().padStart(2, '0')}"
}
