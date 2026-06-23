package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * One salary value that takes effect from a given month onward.
 *
 * [effectiveFromMonth] is a comparable month index = `year * 12 + (monthNumber - 1)`.
 * The salary applicable to any given month is the entry with the largest
 * [effectiveFromMonth] that is still `<= ` that month (see [SalaryCalculator.salaryForMonth]).
 *
 * "Apply to all months" is represented as a single entry whose [effectiveFromMonth] is
 * [SalaryCalculator.ALL_MONTHS] (a baseline that precedes every real month), so it covers
 * past, present and future. "From this month onward" appends/replaces an entry at the
 * current month index, leaving earlier months untouched.
 */
data class SalaryEntry(
    val effectiveFromMonth: Int,
    val amount: Double
)

object SalaryCalculator {
    /** Baseline index used by the "apply to all months" option — precedes every real month. */
    const val ALL_MONTHS: Int = Int.MIN_VALUE

    fun monthIndexOf(year: Int, monthNumber: Int): Int = year * 12 + (monthNumber - 1)

    /** Salary that applies to the given month index, or 0.0 if none has taken effect yet. */
    fun salaryForMonth(history: List<SalaryEntry>, monthIndex: Int): Double =
        history.filter { it.effectiveFromMonth <= monthIndex }
            .maxByOrNull { it.effectiveFromMonth }
            ?.amount ?: 0.0

    /**
     * Salary applicable to the inclusive date range [start]..[end], pro-rated per calendar
     * month by the fraction of that month's days that fall inside the range. This keeps the
     * "saved" figure meaningful for any filter: a full month yields the full salary, a week
     * yields ~1/4, and a whole year sums each month's applicable salary.
     */
    fun salaryForRange(history: List<SalaryEntry>, start: LocalDate, end: LocalDate): Double {
        if (history.isEmpty() || end < start) return 0.0
        var total = 0.0
        var cursor = LocalDate(start.year, start.month, 1)
        while (cursor <= end) {
            val monthSalary = salaryForMonth(history, monthIndexOf(cursor.year, cursor.monthNumber))
            if (monthSalary > 0.0) {
                val monthEnd = cursor.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                val overlapStart = maxOf(cursor, start)
                val overlapEnd = minOf(monthEnd, end)
                val overlapDays = overlapStart.daysUntil(overlapEnd) + 1
                val daysInMonth = cursor.daysUntil(monthEnd) + 1
                total += monthSalary * (overlapDays.toDouble() / daysInMonth)
            }
            cursor = cursor.plus(1, DateTimeUnit.MONTH)
        }
        return total
    }
}
