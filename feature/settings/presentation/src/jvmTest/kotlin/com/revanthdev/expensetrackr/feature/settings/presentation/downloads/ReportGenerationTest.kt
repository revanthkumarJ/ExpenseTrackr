package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the report pipeline end to end on the JVM. The generated files are also written to
 * `build/test-output/` so they can be opened by hand when the layout changes.
 */
class ReportGenerationTest {

    private val outputDir = File("build/test-output").apply { mkdirs() }

    private val sample: ReportData = ReportBuilder.build(
        transactions = sampleTransactions(),
        start = LocalDate(2026, 5, 1),
        end = LocalDate(2026, 7, 31),
        generatedAt = LocalDateTime(2026, 8, 1, 21, 30),
    )

    @Test
    fun `report aggregates income and expense separately`() {
        val expected = sampleTransactions()
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        assertEquals(expected, sample.totalExpense, absoluteTolerance = 0.001)
        assertEquals(sample.totalIncome - sample.totalExpense, sample.net, absoluteTolerance = 0.001)

        // Every expense lands in exactly one category bucket and the shares add up.
        assertEquals(
            sampleTransactions().count { it.type == TransactionType.EXPENSE },
            sample.expenseByCategory.sumOf { it.count },
        )
        assertEquals(100.0, sample.expenseByCategory.sumOf { it.share }, absoluteTolerance = 0.01)
        assertEquals(100.0, sample.expenseBySubCategory.sumOf { it.share }, absoluteTolerance = 0.01)
        // Breakdowns are ordered biggest spend first.
        assertEquals(
            sample.expenseByCategory.map { it.amount }.sortedDescending(),
            sample.expenseByCategory.map { it.amount },
        )
    }

    @Test
    fun `pdf is structurally valid and spans multiple pages`() {
        val bytes = ReportPdf.render(sample)
        File(outputDir, "report.pdf").writeBytes(bytes)
        val text = bytes.decodeToString(throwOnInvalidSequence = false)

        assertTrue(text.startsWith("%PDF-1.4"), "missing PDF header")
        assertTrue(text.trimEnd().endsWith("%%EOF"), "missing EOF marker")

        // 200 sample rows cannot fit on one page, so pagination must have kicked in.
        val pageCount = Regex("/Type /Page[^s]").findAll(text).count()
        assertTrue(pageCount > 1, "expected multiple pages, got $pageCount")
        assertTrue(text.contains("Page 1 of $pageCount"), "footer page numbering is wrong")

        // The cross-reference offset must point at the real xref table, or readers reject the file.
        val startxref = text.substringAfterLast("startxref").trim().substringBefore("%%EOF").trim()
        assertEquals("xref", text.substring(startxref.toInt(), startxref.toInt() + 4))

        assertTrue(text.contains("Category-wise Spending"))
        assertTrue(text.contains("Sub-Category-wise Spending"))
        assertTrue(text.contains("Income by Category"))
    }

    @Test
    fun `a handful of transactions produces a compact report`() {
        val data = ReportBuilder.build(
            transactions = sampleTransactions().filter { it.type == TransactionType.EXPENSE }.take(2),
            start = LocalDate(2026, 5, 1),
            end = LocalDate(2026, 5, 31),
            generatedAt = LocalDateTime(2026, 8, 1, 21, 30),
        )
        val bytes = ReportPdf.render(data)
        File(outputDir, "report-short.pdf").writeBytes(bytes)
        val text = bytes.decodeToString(throwOnInvalidSequence = false)

        assertTrue(text.contains("Page 1 of 1"), "a two-row report should fit on one page")
        // With no income rows the income breakdown is omitted entirely.
        assertTrue(!text.contains("Income by Category"))
    }

    @Test
    fun `pdf escapes text and drops glyphs the base fonts cannot render`() {
        val data = ReportBuilder.build(
            transactions = listOf(
                transaction(
                    name = "Rent (flat) \\ share — नमस्ते",
                    amount = 100.0,
                    category = "Home",
                    subCategory = null,
                    day = 3,
                ),
            ),
            start = LocalDate(2026, 5, 1),
            end = LocalDate(2026, 5, 31),
            generatedAt = LocalDateTime(2026, 8, 1, 9, 0),
        )
        val text = ReportPdf.render(data).decodeToString(throwOnInvalidSequence = false)

        assertTrue(text.contains("""Rent \(flat\) \\ share"""), "parentheses/backslash not escaped")
        // Devanagari has no glyph in Helvetica and must degrade to '?' rather than corrupt the file.
        assertTrue(text.contains("?????"), "unsupported glyphs were not substituted")
    }

    @Test
    fun `xlsx is a readable archive with the expected sheets`() {
        val bytes = ReportXlsx.render(sample)
        val file = File(outputDir, "report.xlsx")
        file.writeBytes(bytes)

        // java.util.zip validates CRCs and the central directory — a strict structural check.
        ZipFile(file).use { zip ->
            val names = zip.entries().toList().map { it.name }
            assertTrue(names.containsAll(
                listOf(
                    "[Content_Types].xml",
                    "_rels/.rels",
                    "xl/workbook.xml",
                    "xl/_rels/workbook.xml.rels",
                    "xl/styles.xml",
                    "xl/worksheets/sheet1.xml",
                    "xl/worksheets/sheet2.xml",
                    "xl/worksheets/sheet3.xml",
                ),
            ), "missing parts: $names")

            val sheet = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml"))
                .readBytes().decodeToString()
            assertTrue(sheet.contains("""<autoFilter"""))
            assertTrue(sheet.contains("""state="frozen""""))
            // Amounts are numbers, not strings, so Excel can sum and chart them.
            assertTrue(Regex("""<c r="D\d+" s="\d+"><v>[\d.]+</v></c>""").containsMatchIn(sheet))

            val workbook = zip.getInputStream(zip.getEntry("xl/workbook.xml")).readBytes().decodeToString()
            assertTrue(workbook.contains("""name="Transactions""""))
            assertTrue(workbook.contains("""name="Category Summary""""))
            assertTrue(workbook.contains("""name="Sub-Category Summary""""))
        }
    }

    @Test
    fun `xlsx escapes characters that would break the XML`() {
        val data = ReportBuilder.build(
            transactions = listOf(
                transaction("Tom & Jerry <toys>", 50.0, "Kids & Family", "\"Gifts\"", day = 4),
            ),
            start = LocalDate(2026, 5, 1),
            end = LocalDate(2026, 5, 31),
            generatedAt = LocalDateTime(2026, 8, 1, 9, 0),
        )
        val file = File(outputDir, "escaping.xlsx").apply { writeBytes(ReportXlsx.render(data)) }
        ZipFile(file).use { zip ->
            val sheet = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml"))
                .readBytes().decodeToString()
            assertTrue(sheet.contains("Tom &amp; Jerry &lt;toys&gt;"))
            assertTrue(sheet.contains("&quot;Gifts&quot;"))
            // Rupee symbols survive because the workbook is UTF-8 throughout.
            val styles = zip.getInputStream(zip.getEntry("xl/styles.xml")).readBytes().decodeToString()
            assertTrue(styles.contains("₹"))
        }
    }

    @Test
    fun `periods resolve to the ranges their labels promise`() {
        val today = LocalDate(2026, 8, 14)
        assertEquals(
            LocalDate(2026, 8, 1) to LocalDate(2026, 8, 31),
            ReportPeriod.ThisMonth.resolve(today),
        )
        assertEquals(
            LocalDate(2026, 7, 1) to LocalDate(2026, 7, 31),
            ReportPeriod.LastMonth.resolve(today),
        )
        assertEquals(LocalDate(2026, 5, 14) to today, ReportPeriod.LastThreeMonths.resolve(today))
        assertEquals(LocalDate(2025, 8, 14) to today, ReportPeriod.LastYear.resolve(today))
        // A backwards custom range is normalised rather than producing an empty report.
        assertEquals(
            LocalDate(2026, 1, 5) to LocalDate(2026, 3, 9),
            ReportPeriod.Custom(LocalDate(2026, 3, 9), LocalDate(2026, 1, 5)).resolve(today),
        )
    }

    @Test
    fun `february is handled when this month is resolved from a 31 day month`() {
        // LocalDate(y, m, 1).plus(1 month).minus(1 day) must not overflow on short months.
        assertEquals(
            LocalDate(2026, 2, 1) to LocalDate(2026, 2, 28),
            ReportPeriod.ThisMonth.resolve(LocalDate(2026, 2, 15)),
        )
        assertEquals(
            LocalDate(2026, 1, 1) to LocalDate(2026, 1, 31),
            ReportPeriod.LastMonth.resolve(LocalDate(2026, 2, 28)),
        )
    }

    // ---- fixtures ----

    private fun sampleTransactions(): List<ExpenseWithDetails> {
        val names = listOf("Groceries", "Metro card", "Coffee", "Electricity", "Movie night")
        val categories = listOf(
            "Food" to listOf("Groceries", "Dining out"),
            "Transport" to listOf("Public transit", null),
            "Bills" to listOf("Utilities", "Internet"),
            "Entertainment" to listOf(null, "Streaming"),
        )
        val expenses = (0 until 200).map { index ->
            val (category, subs) = categories[index % categories.size]
            transaction(
                name = "${names[index % names.size]} #$index",
                amount = 120.0 + index * 37.5,
                category = category,
                subCategory = subs[index % subs.size],
                day = index % 28 + 1,
                month = 5 + index % 3,
            )
        }
        val incomes = (0 until 4).map { index ->
            transaction(
                name = "Salary ${index + 1}",
                amount = 65_000.0 + index * 1_500,
                category = "Salary",
                subCategory = null,
                day = 1,
                month = 5 + index % 3,
                type = TransactionType.INCOME,
            )
        }
        return expenses + incomes
    }

    private fun transaction(
        name: String,
        amount: Double,
        category: String,
        subCategory: String?,
        day: Int,
        month: Int = 5,
        type: TransactionType = TransactionType.EXPENSE,
    ): ExpenseWithDetails {
        val timestamp = LocalDateTime(2026, month, day, 10, 15)
        return ExpenseWithDetails(
            id = 0,
            name = name,
            amount = amount,
            category = Category(
                name = category,
                icon = "🏷️",
                colorHex = "#607D8B",
                type = type,
                createdAt = timestamp,
            ),
            subCategory = subCategory?.let {
                SubCategory(name = it, categoryId = 1, createdAt = timestamp)
            },
            notes = null,
            type = type,
            expenseDate = timestamp,
            createdAt = timestamp,
        )
    }
}
