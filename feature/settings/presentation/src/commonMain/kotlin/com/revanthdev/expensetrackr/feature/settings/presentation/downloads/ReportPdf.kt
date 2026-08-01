package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.PdfColor
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.PdfText
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format.PdfWriter

/**
 * Lays a [ReportData] out as a printable PDF: a titled cover band, a summary of income/expense/net,
 * the full transaction table, and category- and sub-category-wise breakdowns at the end.
 *
 * A4 landscape is used so the six transaction columns stay comfortably readable.
 */
internal object ReportPdf {

    private const val PAGE_WIDTH = 842f
    private const val PAGE_HEIGHT = 595f
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - 46f

    private const val ROW_HEIGHT = 17f
    private const val HEADER_HEIGHT = 21f
    private const val BODY_SIZE = 8.5f

    private val PRIMARY = PdfColor.of(59, 91, 165)
    private val INCOME = PdfColor.of(27, 138, 61)
    private val NEGATIVE = PdfColor.of(198, 40, 40)
    private val INK = PdfColor.of(0, 0, 0)
    private val MUTED = PdfColor.of(107, 114, 128)
    private val ZEBRA = PdfColor.of(246, 248, 252)
    private val CARD = PdfColor.of(240, 244, 250)

    fun render(data: ReportData): ByteArray {
        val pdf = PdfWriter(PAGE_WIDTH, PAGE_HEIGHT)
        Layout(pdf, data).render()
        return pdf.build()
    }

    private class Column(
        val title: String,
        val width: Float,
        val align: Align = Align.LEFT,
    )

    private enum class Align { LEFT, RIGHT, CENTER }

    private class Cell(
        val text: String,
        val color: PdfColor = INK,
        val bold: Boolean = false,
    )

    private class Layout(private val pdf: PdfWriter, private val data: ReportData) {

        private var y = 0f

        fun render() {
            drawBanner(first = true)
            drawSummaryCards()
            drawTransactions()
            drawBreakdowns()
            drawFooters()
        }

        // ---- page furniture ----

        private fun drawBanner(first: Boolean) {
            val height = if (first) 62f else 40f
            pdf.rect(0f, 0f, PAGE_WIDTH, height, PRIMARY)
            pdf.text(
                "Expense Report",
                MARGIN,
                if (first) 32f else 26f,
                if (first) 20f else 14f,
                bold = true,
                color = PdfColor.WHITE,
            )
            if (first) {
                pdf.text(data.rangeLabel, MARGIN, 50f, 10f, color = PdfColor.WHITE)
                pdf.textRight(
                    "Generated ${data.generatedAtLabel}",
                    PAGE_WIDTH - MARGIN,
                    50f,
                    9f,
                    color = PdfColor.WHITE,
                )
            } else {
                pdf.textRight(data.rangeLabel, PAGE_WIDTH - MARGIN, 26f, 9f, color = PdfColor.WHITE)
            }
            y = height + 22f
        }

        private fun newPage() {
            pdf.newPage()
            drawBanner(first = false)
        }

        /** Starts a new page when [needed] points of vertical space are no longer available. */
        private fun ensureSpace(needed: Float) {
            if (y + needed > CONTENT_BOTTOM) newPage()
        }

        private fun drawFooters() {
            val total = pdf.pageCount
            for (index in 0 until total) {
                pdf.selectPage(index)
                val baseline = PAGE_HEIGHT - 22f
                pdf.hLine(baseline - 12f, MARGIN, PAGE_WIDTH - MARGIN)
                pdf.text("ExpenseTrackr", MARGIN, baseline, 8f, color = MUTED)
                pdf.textCenter("Page ${index + 1} of $total", PAGE_WIDTH / 2f, baseline, 8f, color = MUTED)
                pdf.textRight(data.rangeLabel, PAGE_WIDTH - MARGIN, baseline, 8f, color = MUTED)
            }
        }

        // ---- sections ----

        private fun drawSummaryCards() {
            val gap = 12f
            val width = (CONTENT_WIDTH - gap * 2) / 3f
            val height = 58f
            val cards = listOf(
                Triple("TOTAL INCOME", data.totalIncome.toPdfAmount(), INCOME),
                Triple("TOTAL EXPENSE", data.totalExpense.toPdfAmount(), INK),
                Triple(
                    "NET BALANCE",
                    data.net.toPdfAmount(),
                    if (data.net < 0) NEGATIVE else INCOME,
                ),
            )
            cards.forEachIndexed { index, (label, value, color) ->
                val x = MARGIN + index * (width + gap)
                pdf.roundedRect(x, y, width, height, 6f, CARD)
                pdf.text(label, x + 14f, y + 21f, 8f, bold = true, color = MUTED)
                pdf.text(value, x + 14f, y + 44f, 15f, bold = true, color = color)
            }
            y += height + 14f

            pdf.text(
                "${data.rows.size} transactions in this period",
                MARGIN,
                y + 8f,
                9f,
                color = MUTED,
            )
            y += 22f
        }

        private fun drawTransactions() {
            // Widths add up to CONTENT_WIDTH (770pt); Date is sized to hold "01 May 2026 10:15 AM".
            val columns = listOf(
                Column("Date", 124f),
                Column("Expense Name", 180f),
                Column("Type", 60f, Align.CENTER),
                Column("Amount", 102f, Align.RIGHT),
                Column("Category", 150f),
                Column("Sub-Category", 154f),
            )
            val rows = data.rows.map { row ->
                val isIncome = row.type == TransactionType.INCOME
                val color = if (isIncome) INCOME else INK
                listOf(
                    Cell("${formatReportDate(row.date.date)} ${formatReportTime(row.date)}"),
                    Cell(row.name),
                    Cell(if (isIncome) "Income" else "Expense", color),
                    // Income is added, expense is taken away — the sign makes that explicit.
                    Cell(
                        (if (isIncome) "+ " else "- ") + row.amount.toPdfAmount(),
                        color,
                        bold = true,
                    ),
                    Cell(row.category),
                    Cell(row.subCategory ?: "-", if (row.subCategory == null) MUTED else INK),
                )
            }
            table("Transactions", columns, rows)
        }

        private fun drawBreakdowns() {
            if (data.expenseByCategory.isNotEmpty()) {
                table(
                    title = "Category-wise Spending",
                    columns = listOf(
                        Column("Category", 300f),
                        Column("Transactions", 110f, Align.CENTER),
                        Column("Amount Spent", 140f, Align.RIGHT),
                        Column("% of Spend", 110f, Align.RIGHT),
                    ),
                    rows = data.expenseByCategory.map { group ->
                        listOf(
                            Cell(group.label),
                            Cell(group.count.toString()),
                            Cell(group.amount.toPdfAmount(), bold = true),
                            Cell(group.share.toPercentString(), color = MUTED),
                        )
                    },
                    totalRow = listOf(
                        Cell("Total", bold = true),
                        Cell(data.expenseByCategory.sumOf { it.count }.toString(), bold = true),
                        Cell(data.totalExpense.toPdfAmount(), bold = true),
                        Cell("100.00%", bold = true),
                    ),
                )
            }

            if (data.expenseBySubCategory.isNotEmpty()) {
                table(
                    title = "Sub-Category-wise Spending",
                    columns = listOf(
                        Column("Category", 220f),
                        Column("Sub-Category", 220f),
                        Column("Transactions", 100f, Align.CENTER),
                        Column("Amount Spent", 130f, Align.RIGHT),
                        Column("% of Spend", 100f, Align.RIGHT),
                    ),
                    rows = data.expenseBySubCategory.map { group ->
                        listOf(
                            Cell(group.parent.orEmpty(), color = MUTED),
                            Cell(group.label),
                            Cell(group.count.toString()),
                            Cell(group.amount.toPdfAmount(), bold = true),
                            Cell(group.share.toPercentString(), color = MUTED),
                        )
                    },
                    totalRow = listOf(
                        Cell("Total", bold = true),
                        Cell(""),
                        Cell(data.expenseBySubCategory.sumOf { it.count }.toString(), bold = true),
                        Cell(data.totalExpense.toPdfAmount(), bold = true),
                        Cell("100.00%", bold = true),
                    ),
                )
            }

            if (data.incomeByCategory.isNotEmpty()) {
                table(
                    title = "Income by Category",
                    columns = listOf(
                        Column("Category", 300f),
                        Column("Transactions", 110f, Align.CENTER),
                        Column("Amount Received", 140f, Align.RIGHT),
                        Column("% of Income", 110f, Align.RIGHT),
                    ),
                    rows = data.incomeByCategory.map { group ->
                        listOf(
                            Cell(group.label),
                            Cell(group.count.toString()),
                            Cell(group.amount.toPdfAmount(), INCOME, bold = true),
                            Cell(group.share.toPercentString(), color = MUTED),
                        )
                    },
                    totalRow = listOf(
                        Cell("Total", bold = true),
                        Cell(data.incomeByCategory.sumOf { it.count }.toString(), bold = true),
                        Cell(data.totalIncome.toPdfAmount(), INCOME, bold = true),
                        Cell("100.00%", bold = true),
                    ),
                )
            }
        }

        // ---- table engine ----

        /**
         * Draws a section heading followed by a paginated table. The column header repeats at the
         * top of every page the table spills onto.
         */
        private fun table(
            title: String,
            columns: List<Column>,
            rows: List<List<Cell>>,
            totalRow: List<Cell>? = null,
        ) {
            // Keep the heading with at least a couple of rows rather than stranding it.
            ensureSpace(28f + HEADER_HEIGHT + ROW_HEIGHT * 2)
            pdf.text(title, MARGIN, y + 11f, 12f, bold = true, color = PRIMARY)
            y += 20f

            drawColumnHeader(columns)
            if (rows.isEmpty()) {
                pdf.text("No transactions in this period.", MARGIN + 6f, y + 12f, BODY_SIZE, color = MUTED)
                y += ROW_HEIGHT + 14f
                return
            }

            rows.forEachIndexed { index, cells ->
                if (y + ROW_HEIGHT > CONTENT_BOTTOM) {
                    newPage()
                    drawColumnHeader(columns)
                }
                if (index % 2 == 1) pdf.rect(MARGIN, y, CONTENT_WIDTH, ROW_HEIGHT, ZEBRA)
                drawCells(columns, cells, ROW_HEIGHT, bold = false)
                y += ROW_HEIGHT
            }

            pdf.hLine(y, MARGIN, MARGIN + CONTENT_WIDTH, 0.8f, PRIMARY)

            if (totalRow != null) {
                if (y + ROW_HEIGHT + 2f > CONTENT_BOTTOM) {
                    newPage()
                    drawColumnHeader(columns)
                }
                y += 2f
                drawCells(columns, totalRow, ROW_HEIGHT, bold = true)
                y += ROW_HEIGHT
            }
            y += 22f
        }

        private fun drawColumnHeader(columns: List<Column>) {
            pdf.rect(MARGIN, y, CONTENT_WIDTH, HEADER_HEIGHT, PRIMARY)
            drawCells(
                columns,
                columns.map { Cell(it.title, PdfColor.WHITE, bold = true) },
                HEADER_HEIGHT,
                bold = true,
            )
            y += HEADER_HEIGHT
        }

        private fun drawCells(
            columns: List<Column>,
            cells: List<Cell>,
            height: Float,
            bold: Boolean,
        ) {
            val baseline = y + height / 2f + BODY_SIZE * 0.36f
            var x = MARGIN
            columns.forEachIndexed { index, column ->
                val cell = cells.getOrNull(index) ?: return@forEachIndexed
                val isBold = bold || cell.bold
                val padding = 6f
                val available = column.width - padding * 2
                val text = PdfText.fit(cell.text, available, BODY_SIZE, isBold)
                when (column.align) {
                    Align.LEFT -> pdf.text(text, x + padding, baseline, BODY_SIZE, isBold, cell.color)
                    Align.RIGHT ->
                        pdf.textRight(text, x + column.width - padding, baseline, BODY_SIZE, isBold, cell.color)
                    Align.CENTER ->
                        pdf.textCenter(text, x + column.width / 2f, baseline, BODY_SIZE, isBold, cell.color)
                }
                x += column.width
            }
        }
    }
}
