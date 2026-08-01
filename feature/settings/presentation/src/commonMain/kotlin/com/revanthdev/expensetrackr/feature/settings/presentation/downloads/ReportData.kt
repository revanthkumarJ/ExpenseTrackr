package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Which file the user asked for. Public because it appears in the screen's state and actions. */
enum class ReportFormat(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
}

/**
 * The stretch of time a report covers. Every option resolves to a concrete start/end date, which
 * is then reused as a [DateFilter.CustomRange] so the report goes through the same query path as
 * the rest of the app.
 */
sealed interface ReportPeriod {
    data object ThisMonth : ReportPeriod
    data object LastMonth : ReportPeriod
    data object LastThreeMonths : ReportPeriod
    data object LastYear : ReportPeriod
    data class Custom(val start: LocalDate, val end: LocalDate) : ReportPeriod

    /** Resolves to an inclusive [start, end] day range relative to [today]. */
    fun resolve(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        ThisMonth -> {
            val start = LocalDate(today.year, today.month, 1)
            start to start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        }

        LastMonth -> {
            val previous = LocalDate(today.year, today.month, 1).minus(1, DateTimeUnit.MONTH)
            previous to previous.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        }

        // Rolling windows ending today; the exact dates are always printed on the report.
        LastThreeMonths -> today.minus(3, DateTimeUnit.MONTH) to today
        LastYear -> today.minus(1, DateTimeUnit.YEAR) to today
        is Custom -> minOf(start, end) to maxOf(start, end)
    }
}

internal data class ReportRow(
    val date: LocalDateTime,
    val name: String,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val subCategory: String?,
)

/** One line of a category / sub-category breakdown. [share] is a percentage of the group total. */
internal data class GroupTotal(
    val label: String,
    val parent: String?,
    val amount: Double,
    val count: Int,
    val share: Double,
)

internal data class ReportData(
    val rangeLabel: String,
    val generatedAtLabel: String,
    val rows: List<ReportRow>,
    val totalIncome: Double,
    val totalExpense: Double,
    val expenseByCategory: List<GroupTotal>,
    val expenseBySubCategory: List<GroupTotal>,
    val incomeByCategory: List<GroupTotal>,
) {
    val net: Double get() = totalIncome - totalExpense
    val isEmpty: Boolean get() = rows.isEmpty()
}

internal object ReportBuilder {

    const val UNCATEGORISED = "Uncategorised"

    fun build(
        transactions: List<ExpenseWithDetails>,
        start: LocalDate,
        end: LocalDate,
        generatedAt: LocalDateTime,
    ): ReportData {
        val rows = transactions
            .sortedBy { it.expenseDate }
            .map { transaction ->
                ReportRow(
                    date = transaction.expenseDate,
                    name = transaction.name,
                    type = transaction.type,
                    amount = transaction.amount,
                    category = transaction.category.name,
                    subCategory = transaction.subCategory?.name,
                )
            }

        val expenses = rows.filter { it.type == TransactionType.EXPENSE }
        val incomes = rows.filter { it.type == TransactionType.INCOME }
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        return ReportData(
            rangeLabel = "${formatReportDate(start)} to ${formatReportDate(end)}",
            generatedAtLabel = "${formatReportDate(generatedAt.date)}, ${formatReportTime(generatedAt)}",
            rows = rows,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            expenseByCategory = groupBy(expenses, totalExpense) { it.category to null },
            expenseBySubCategory = groupBy(expenses, totalExpense) {
                (it.subCategory ?: UNCATEGORISED) to it.category
            },
            incomeByCategory = groupBy(incomes, totalIncome) { it.category to null },
        )
    }

    /** Sums rows into breakdown lines, biggest first. [key] returns the label and its parent. */
    private inline fun groupBy(
        rows: List<ReportRow>,
        total: Double,
        key: (ReportRow) -> Pair<String, String?>,
    ): List<GroupTotal> = rows
        .groupBy(key)
        .map { (labels, grouped) ->
            val amount = grouped.sumOf { it.amount }
            GroupTotal(
                label = labels.first,
                parent = labels.second,
                amount = amount,
                count = grouped.size,
                share = if (total > 0.0) amount / total * 100.0 else 0.0,
            )
        }
        .sortedWith(compareByDescending<GroupTotal> { it.amount }.thenBy { it.label })
}

private val MONTHS =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "01 Aug 2026" — reports are deliberately English so they read the same for every recipient. */
internal fun formatReportDate(date: LocalDate): String =
    "${date.dayOfMonth.toString().padStart(2, '0')} ${MONTHS[date.monthNumber - 1]} ${date.year}"

internal fun formatReportTime(value: LocalDateTime): String {
    val suffix = if (value.hour < 12) "AM" else "PM"
    val hour = when {
        value.hour == 0 -> 12
        value.hour > 12 -> value.hour - 12
        else -> value.hour
    }
    return "$hour:${value.minute.toString().padStart(2, '0')} $suffix"
}

/**
 * "Rs. 12,34,567.89". The base-14 PDF fonts have no glyph for `₹`, so the PDF spells the currency
 * out; the Excel export keeps the real symbol via a number format.
 */
internal fun Double.toPdfAmount(): String = toCurrencyString().replace("₹", "Rs. ")

/** File name such as `ExpenseReport_2026-08-01_to_2026-08-31.pdf`. */
internal fun reportFileName(start: LocalDate, end: LocalDate, format: ReportFormat): String {
    fun stamp(date: LocalDate) = "${date.year}-" +
        "${date.monthNumber.toString().padStart(2, '0')}-" +
        date.dayOfMonth.toString().padStart(2, '0')
    return "ExpenseReport_${stamp(start)}_to_${stamp(end)}.${format.extension}"
}
