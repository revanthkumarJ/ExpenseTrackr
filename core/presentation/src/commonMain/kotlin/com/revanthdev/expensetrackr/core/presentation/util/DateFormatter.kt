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

/** Groups an integer the Indian way: last 3 digits, then groups of 2 (e.g. 1234567 → 12,34,567). */
private fun groupIndian(intPart: Long): String {
    val s = intPart.toString()
    if (s.length <= 3) return s
    val last3 = s.substring(s.length - 3)
    var rest = s.substring(0, s.length - 3)
    val sb = StringBuilder()
    while (rest.length > 2) {
        sb.insert(0, "," + rest.substring(rest.length - 2))
        rest = rest.substring(0, rest.length - 2)
    }
    sb.insert(0, rest)
    return "$sb,$last3"
}

/** "₹12,34,567.89" — Indian digit grouping, 2 decimals, rounded. */
fun Double.toCurrencyString(): String {
    val negative = this < 0
    val cents = kotlin.math.round(kotlin.math.abs(this) * 100).toLong()
    val intPart = cents / 100
    val frac = cents % 100
    val sign = if (negative) "-" else ""
    return "$sign₹${groupIndian(intPart)}.${frac.toString().padStart(2, '0')}"
}

/**
 * Plain 2-decimal string with no currency symbol or grouping (e.g. 1234.5 → "1234.50").
 * Multiplatform-safe replacement for JVM-only `"%.2f".format(this)` — used to prefill
 * amount text fields.
 */
fun Double.toAmountString(): String {
    val negative = this < 0
    val cents = kotlin.math.round(kotlin.math.abs(this) * 100).toLong()
    val sign = if (negative) "-" else ""
    return "$sign${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}

/** "0.01%", "12.50%" — 2 decimals, rounded (never truncates small values to 0%). */
fun Double.toPercentString(): String {
    val negative = this < 0
    val hundredths = kotlin.math.round(kotlin.math.abs(this) * 100).toLong()
    val intPart = hundredths / 100
    val frac = hundredths % 100
    val sign = if (negative) "-" else ""
    return "$sign$intPart.${frac.toString().padStart(2, '0')}%"
}
