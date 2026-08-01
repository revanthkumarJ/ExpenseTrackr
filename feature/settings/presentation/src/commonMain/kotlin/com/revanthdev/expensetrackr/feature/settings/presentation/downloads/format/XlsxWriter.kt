package com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format

import kotlinx.datetime.LocalDateTime
import kotlin.math.abs
import kotlin.math.round

/**
 * The cell formats the report uses. The ordinal of each entry is its index in `cellXfs` inside
 * [XlsxWriter.STYLES_XML] — keep the two lists in sync.
 */
internal enum class XlsxStyle {
    DEFAULT,
    BOLD,
    TITLE,
    SECTION,
    HEADER,
    MONEY,
    MONEY_INCOME,
    MONEY_BOLD,
    DATE,
    MUTED,
    PERCENT,
    CENTER,
    MONEY_INCOME_BOLD,
}

internal sealed interface XlsxCell {
    val style: XlsxStyle

    data class Text(val value: String, override val style: XlsxStyle = XlsxStyle.DEFAULT) : XlsxCell
    data class Number(val value: Double, override val style: XlsxStyle = XlsxStyle.DEFAULT) : XlsxCell
    data class DateTime(val value: LocalDateTime, override val style: XlsxStyle = XlsxStyle.DATE) : XlsxCell
    data object Empty : XlsxCell {
        override val style = XlsxStyle.DEFAULT
    }
}

internal class XlsxSheet(val name: String) {
    val rows = mutableListOf<List<XlsxCell>>()
    var columnWidths: List<Double> = emptyList()

    /** 1-based row index of the table header; enables a frozen pane and an auto-filter. */
    var headerRow: Int? = null

    /**
     * Last row the auto-filter covers, so totals rows and any second table below stay outside it.
     * Defaults to the final row of the sheet.
     */
    var autoFilterLastRow: Int? = null

    fun row(vararg cells: XlsxCell) {
        rows.add(cells.toList())
    }

    fun blank() {
        rows.add(emptyList())
    }
}

/**
 * Writes a minimal but fully valid `.xlsx` (SpreadsheetML) workbook.
 *
 * Strings are written inline (`t="inlineStr"`) so no shared-string table is needed, and the parts
 * are packed with [ZipBuilder]. Unlike the PDF, this format is UTF-8 throughout, so real `₹`
 * symbols and any script the user types are preserved.
 */
internal object XlsxWriter {

    fun build(sheets: List<XlsxSheet>): ByteArray {
        val zip = ZipBuilder()
        zip.add("[Content_Types].xml", contentTypes(sheets.size))
        zip.add("_rels/.rels", RELS_XML)
        zip.add("xl/workbook.xml", workbook(sheets))
        zip.add("xl/_rels/workbook.xml.rels", workbookRels(sheets.size))
        zip.add("xl/styles.xml", STYLES_XML)
        sheets.forEachIndexed { index, sheet ->
            zip.add("xl/worksheets/sheet${index + 1}.xml", sheetXml(sheet))
        }
        return zip.build()
    }

    // ---- parts ----

    private fun contentTypes(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        append("""<Default Extension="xml" ContentType="application/xml"/>""")
        append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
        for (index in 1..sheetCount) {
            append("""<Override PartName="/xl/worksheets/sheet$index.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
        }
        append("</Types>")
    }

    private const val RELS_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
            """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""" +
            """<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>""" +
            """</Relationships>"""

    private fun workbook(sheets: List<XlsxSheet>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """)
        append("""xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>""")
        sheets.forEachIndexed { index, sheet ->
            append("""<sheet name="${escape(sheet.name)}" sheetId="${index + 1}" r:id="rId${index + 1}"/>""")
        }
        append("</sheets></workbook>")
    }

    private fun workbookRels(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        for (index in 1..sheetCount) {
            append("""<Relationship Id="rId$index" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$index.xml"/>""")
        }
        append("</Relationships>")
    }

    /**
     * Fonts, fills and the `cellXfs` list whose indices [XlsxStyle] maps onto. Custom number
     * formats start at 164 (ids below that are reserved for Excel's built-ins).
     */
    private const val STYLES_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
            """<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""" +
            """<numFmts count="3">""" +
            """<numFmt numFmtId="164" formatCode="&quot;₹&quot;#,##0.00"/>""" +
            """<numFmt numFmtId="165" formatCode="dd\-mmm\-yyyy\ hh:mm"/>""" +
            """<numFmt numFmtId="166" formatCode="0.00&quot;%&quot;"/>""" +
            """</numFmts>""" +
            """<fonts count="9">""" +
            """<font><sz val="10"/><name val="Calibri"/></font>""" +
            """<font><b/><sz val="10"/><name val="Calibri"/></font>""" +
            """<font><b/><sz val="10"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>""" +
            """<font><b/><sz val="16"/><color rgb="FF1B2A4A"/><name val="Calibri"/></font>""" +
            """<font><sz val="10"/><color rgb="FF1B8A3D"/><name val="Calibri"/></font>""" +
            """<font><sz val="10"/><name val="Calibri"/></font>""" +
            """<font><b/><sz val="10"/><color rgb="FF1B8A3D"/><name val="Calibri"/></font>""" +
            """<font><sz val="9"/><color rgb="FF6B7280"/><name val="Calibri"/></font>""" +
            """<font><b/><sz val="12"/><color rgb="FF1B2A4A"/><name val="Calibri"/></font>""" +
            """</fonts>""" +
            """<fills count="3">""" +
            """<fill><patternFill patternType="none"/></fill>""" +
            """<fill><patternFill patternType="gray125"/></fill>""" +
            """<fill><patternFill patternType="solid"><fgColor rgb="FF3B5BA5"/><bgColor indexed="64"/></patternFill></fill>""" +
            """</fills>""" +
            """<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>""" +
            """<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>""" +
            """<cellXfs count="13">""" +
            """<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>""" +
            """<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>""" +
            """<xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1"/>""" +
            """<xf numFmtId="0" fontId="8" fillId="0" borderId="0" xfId="0" applyFont="1"/>""" +
            """<xf numFmtId="0" fontId="2" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment vertical="center"/></xf>""" +
            """<xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>""" +
            """<xf numFmtId="164" fontId="4" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1"/>""" +
            """<xf numFmtId="164" fontId="1" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1"/>""" +
            """<xf numFmtId="165" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>""" +
            """<xf numFmtId="0" fontId="7" fillId="0" borderId="0" xfId="0" applyFont="1"/>""" +
            """<xf numFmtId="166" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>""" +
            """<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center"/></xf>""" +
            """<xf numFmtId="164" fontId="6" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1"/>""" +
            """</cellXfs>""" +
            """<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>""" +
            """</styleSheet>"""

    private fun sheetXml(sheet: XlsxSheet): String = buildString {
        val columnCount = maxOf(sheet.rows.maxOfOrNull { it.size } ?: 1, 1)
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("""<dimension ref="A1:${column(columnCount - 1)}${maxOf(sheet.rows.size, 1)}"/>""")

        append("""<sheetViews><sheetView workbookViewId="0">""")
        sheet.headerRow?.let { header ->
            append("""<pane ySplit="$header" topLeftCell="A${header + 1}" activePane="bottomLeft" state="frozen"/>""")
        }
        append("""</sheetView></sheetViews>""")
        append("""<sheetFormatPr defaultRowHeight="15"/>""")

        if (sheet.columnWidths.isNotEmpty()) {
            append("<cols>")
            sheet.columnWidths.forEachIndexed { index, width ->
                append("""<col min="${index + 1}" max="${index + 1}" width="${plain(width, 2)}" customWidth="1"/>""")
            }
            append("</cols>")
        }

        append("<sheetData>")
        sheet.rows.forEachIndexed { rowIndex, cells ->
            val rowNumber = rowIndex + 1
            if (cells.isEmpty()) {
                append("""<row r="$rowNumber"/>""")
                return@forEachIndexed
            }
            append("""<row r="$rowNumber">""")
            cells.forEachIndexed { columnIndex, cell ->
                if (cell is XlsxCell.Empty) return@forEachIndexed
                val reference = "${column(columnIndex)}$rowNumber"
                val style = cell.style.ordinal
                when (cell) {
                    is XlsxCell.Text ->
                        append("""<c r="$reference" s="$style" t="inlineStr"><is><t xml:space="preserve">${escape(cell.value)}</t></is></c>""")
                    is XlsxCell.Number ->
                        append("""<c r="$reference" s="$style"><v>${plain(cell.value, 4)}</v></c>""")
                    is XlsxCell.DateTime ->
                        append("""<c r="$reference" s="$style"><v>${plain(excelSerial(cell.value), 6)}</v></c>""")
                    XlsxCell.Empty -> Unit
                }
            }
            append("</row>")
        }
        append("</sheetData>")

        // autoFilter must follow sheetData per the schema's element order.
        sheet.headerRow?.let { header ->
            val lastRow = maxOf(sheet.autoFilterLastRow ?: sheet.rows.size, header)
            append("""<autoFilter ref="A$header:${column(columnCount - 1)}$lastRow"/>""")
        }
        append("</worksheet>")
    }

    // ---- helpers ----

    /** 0 -> "A", 25 -> "Z", 26 -> "AA". */
    private fun column(index: Int): String {
        var remaining = index
        val sb = StringBuilder()
        while (true) {
            sb.insert(0, ('A' + remaining % 26))
            remaining = remaining / 26 - 1
            if (remaining < 0) break
        }
        return sb.toString()
    }

    /** Excel stores dates as days since 1899-12-30, with the time of day as the fraction. */
    private fun excelSerial(value: LocalDateTime): Double {
        val days = value.date.toEpochDays() + 25569L
        val seconds = value.hour * 3600 + value.minute * 60 + value.second
        return days + seconds / 86_400.0
    }

    /** Decimal string with no exponent — Excel is strict about the `<v>` payload. */
    private fun plain(value: Double, decimals: Int): String {
        var factor = 1L
        repeat(decimals) { factor *= 10 }
        val scaled = round(abs(value) * factor).toLong()
        val whole = scaled / factor
        val fraction = (scaled % factor).toString().padStart(decimals, '0').trimEnd('0')
        val sign = if (value < 0 && scaled != 0L) "-" else ""
        return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
    }

    private fun escape(text: String): String {
        val sb = StringBuilder(text.length + 8)
        for (char in text) {
            when {
                char == '&' -> sb.append("&amp;")
                char == '<' -> sb.append("&lt;")
                char == '>' -> sb.append("&gt;")
                char == '"' -> sb.append("&quot;")
                char == '\'' -> sb.append("&apos;")
                // Control characters are illegal in XML 1.0 even when escaped.
                char.code < 0x20 && char != '\n' && char != '\t' -> sb.append(' ')
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }
}
