package com.revanthdev.expensetrackr.core.data.datasource

import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class DateRange(val startMs: Long, val endMs: Long)

fun DateFilter.toDateRange(): DateRange {
    val tz = TimeZone.currentSystemDefault()
    val now = kotlin.time.Clock.System.now().toLocalDateTime(tz)
    val today = now.date

    return when (this) {
        DateFilter.ThisMonth -> {
            val start = LocalDate(today.year, today.month, 1)
            val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            DateRange(
                startMs = start.atStartOfDayIn(tz).toEpochMilliseconds(),
                endMs = end.atTime(LocalTime(23, 59, 59)).toInstant(tz).toEpochMilliseconds()
            )
        }

        DateFilter.LastMonth -> {
            val lastMonth = today.minus(1, DateTimeUnit.MONTH)
            val start = LocalDate(lastMonth.year, lastMonth.month, 1)
            val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            DateRange(
                startMs = start.atStartOfDayIn(tz).toEpochMilliseconds(),
                endMs = end.atTime(LocalTime(23, 59, 59)).toInstant(tz).toEpochMilliseconds()
            )
        }

        DateFilter.ThisWeek -> {
            // DayOfWeek.MONDAY.ordinal == 0 in kotlinx.datetime (ISO-8601)
            val dayOfWeek = today.dayOfWeek.ordinal
            val monday = today.minus(dayOfWeek, DateTimeUnit.DAY)
            val sunday = monday.plus(6, DateTimeUnit.DAY)
            DateRange(
                startMs = monday.atStartOfDayIn(tz).toEpochMilliseconds(),
                endMs = sunday.atTime(LocalTime(23, 59, 59)).toInstant(tz).toEpochMilliseconds()
            )
        }

        DateFilter.ThisYear -> {
            val start = LocalDate(today.year, 1, 1)
            val end = LocalDate(today.year, 12, 31)
            DateRange(
                startMs = start.atStartOfDayIn(tz).toEpochMilliseconds(),
                endMs = end.atTime(LocalTime(23, 59, 59)).toInstant(tz).toEpochMilliseconds()
            )
        }

        is DateFilter.CustomRange -> DateRange(
            startMs = start.atStartOfDayIn(tz).toEpochMilliseconds(),
            endMs = end.atTime(LocalTime(23, 59, 59)).toInstant(tz).toEpochMilliseconds()
        )
    }
}
