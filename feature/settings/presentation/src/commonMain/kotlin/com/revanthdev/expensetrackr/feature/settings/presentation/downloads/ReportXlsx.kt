package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.XlsxCell
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.XlsxSheet
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.XlsxStyle
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.XlsxWriter

/**
 * Lays a [ReportData] out as a three-sheet workbook: the transaction ledger, a category summary
 * and a sub-category summary.
 *
 * Amounts and dates are written as real numbers with number formats attached, so the sheets stay
 * sortable and can be charted or pivoted — and the `₹` symbol survives, unlike in the PDF.
 */
internal object ReportXlsx {

    fun render(data: ReportData): ByteArray = XlsxWriter.build(
        listOf(transactionsSheet(data), categorySheet(data), subCategorySheet(data)),
    )

    private fun transactionsSheet(data: ReportData) = XlsxSheet("Transactions").apply {
        columnWidths = listOf(20.0, 34.0, 12.0, 16.0, 22.0, 22.0)

        row(text("Expense Report", XlsxStyle.TITLE))
        row(text("Period", XlsxStyle.BOLD), text(data.rangeLabel))
        row(text("Generated", XlsxStyle.BOLD), text(data.generatedAtLabel, XlsxStyle.MUTED))
        blank()
        row(text("Total Income", XlsxStyle.BOLD), money(data.totalIncome, XlsxStyle.MONEY_INCOME_BOLD))
        row(text("Total Expense", XlsxStyle.BOLD), money(data.totalExpense, XlsxStyle.MONEY_BOLD))
        row(text("Net Balance", XlsxStyle.BOLD), money(data.net, XlsxStyle.MONEY_BOLD))
        row(text("Transactions", XlsxStyle.BOLD), XlsxCell.Number(data.rows.size.toDouble()))
        blank()

        headerRow = rows.size + 1
        row(
            text("Date", XlsxStyle.HEADER),
            text("Expense Name", XlsxStyle.HEADER),
            text("Type", XlsxStyle.HEADER),
            text("Amount", XlsxStyle.HEADER),
            text("Category", XlsxStyle.HEADER),
            text("Sub-Category", XlsxStyle.HEADER),
        )

        data.rows.forEach { entry ->
            val isIncome = entry.type == TransactionType.INCOME
            row(
                XlsxCell.DateTime(entry.date),
                text(entry.name),
                text(if (isIncome) "Income" else "Expense", XlsxStyle.CENTER),
                // Income is shown in green, expense in the default black.
                money(entry.amount, if (isIncome) XlsxStyle.MONEY_INCOME else XlsxStyle.MONEY),
                text(entry.category),
                text(entry.subCategory ?: "-"),
            )
        }
    }

    private fun categorySheet(data: ReportData) = XlsxSheet("Category Summary").apply {
        columnWidths = listOf(28.0, 14.0, 18.0, 14.0)

        row(text("Category-wise Spending", XlsxStyle.TITLE))
        row(text(data.rangeLabel, XlsxStyle.MUTED))
        blank()

        headerRow = rows.size + 1
        row(
            text("Category", XlsxStyle.HEADER),
            text("Transactions", XlsxStyle.HEADER),
            text("Amount Spent", XlsxStyle.HEADER),
            text("% of Spend", XlsxStyle.HEADER),
        )
        data.expenseByCategory.forEach { group ->
            row(
                text(group.label),
                XlsxCell.Number(group.count.toDouble(), XlsxStyle.CENTER),
                money(group.amount),
                XlsxCell.Number(group.share, XlsxStyle.PERCENT),
            )
        }
        autoFilterLastRow = rows.size
        row(
            text("Total", XlsxStyle.BOLD),
            XlsxCell.Number(data.expenseByCategory.sumOf { it.count }.toDouble(), XlsxStyle.CENTER),
            money(data.totalExpense, XlsxStyle.MONEY_BOLD),
            XlsxCell.Number(if (data.totalExpense > 0) 100.0 else 0.0, XlsxStyle.PERCENT),
        )

        if (data.incomeByCategory.isNotEmpty()) {
            blank()
            row(text("Income by Category", XlsxStyle.SECTION))
            row(
                text("Category", XlsxStyle.HEADER),
                text("Transactions", XlsxStyle.HEADER),
                text("Amount Received", XlsxStyle.HEADER),
                text("% of Income", XlsxStyle.HEADER),
            )
            data.incomeByCategory.forEach { group ->
                row(
                    text(group.label),
                    XlsxCell.Number(group.count.toDouble(), XlsxStyle.CENTER),
                    money(group.amount, XlsxStyle.MONEY_INCOME),
                    XlsxCell.Number(group.share, XlsxStyle.PERCENT),
                )
            }
            row(
                text("Total", XlsxStyle.BOLD),
                XlsxCell.Number(data.incomeByCategory.sumOf { it.count }.toDouble(), XlsxStyle.CENTER),
                money(data.totalIncome, XlsxStyle.MONEY_INCOME_BOLD),
                XlsxCell.Number(if (data.totalIncome > 0) 100.0 else 0.0, XlsxStyle.PERCENT),
            )
        }
    }

    private fun subCategorySheet(data: ReportData) = XlsxSheet("Sub-Category Summary").apply {
        columnWidths = listOf(24.0, 26.0, 14.0, 18.0, 14.0)

        row(text("Sub-Category-wise Spending", XlsxStyle.TITLE))
        row(text(data.rangeLabel, XlsxStyle.MUTED))
        blank()

        headerRow = rows.size + 1
        row(
            text("Category", XlsxStyle.HEADER),
            text("Sub-Category", XlsxStyle.HEADER),
            text("Transactions", XlsxStyle.HEADER),
            text("Amount Spent", XlsxStyle.HEADER),
            text("% of Spend", XlsxStyle.HEADER),
        )
        data.expenseBySubCategory.forEach { group ->
            row(
                text(group.parent.orEmpty()),
                text(group.label),
                XlsxCell.Number(group.count.toDouble(), XlsxStyle.CENTER),
                money(group.amount),
                XlsxCell.Number(group.share, XlsxStyle.PERCENT),
            )
        }
        autoFilterLastRow = rows.size
        row(
            text("Total", XlsxStyle.BOLD),
            XlsxCell.Empty,
            XlsxCell.Number(data.expenseBySubCategory.sumOf { it.count }.toDouble(), XlsxStyle.CENTER),
            money(data.totalExpense, XlsxStyle.MONEY_BOLD),
            XlsxCell.Number(if (data.totalExpense > 0) 100.0 else 0.0, XlsxStyle.PERCENT),
        )
    }

    private fun text(value: String, style: XlsxStyle = XlsxStyle.DEFAULT) = XlsxCell.Text(value, style)

    private fun money(value: Double, style: XlsxStyle = XlsxStyle.MONEY) = XlsxCell.Number(value, style)
}
