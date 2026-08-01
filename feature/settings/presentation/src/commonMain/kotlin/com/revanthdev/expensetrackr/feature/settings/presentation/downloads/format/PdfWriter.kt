package com.revanthdev.expensetrackr.feature.settings.presentation.downloads.format

import kotlin.math.abs
import kotlin.math.round

/**
 * A tiny, dependency-free PDF 1.4 writer good enough for tabular reports.
 *
 * It emits uncompressed content streams and uses the two base-14 fonts every PDF reader has
 * built in (Helvetica and Helvetica-Bold), so nothing has to be embedded.
 *
 * ### Coordinates
 * PDF's own origin is the bottom-left corner with y growing upwards. This class exposes the
 * screen-like convention instead — origin **top-left**, y grows **downwards** — and flips
 * internally, so layout code reads naturally.
 *
 * ### Text encoding
 * Text is written with `WinAnsiEncoding`, which covers ASCII and Latin-1. Characters outside it
 * (Devanagari, CJK, the ₹ sign …) have no glyph in the base-14 fonts and are replaced with `?`;
 * see [PdfText.toWinAnsi]. Report code therefore spells amounts as `Rs.` rather than `₹`.
 */
internal class PdfWriter(
    private val pageWidth: Float,
    private val pageHeight: Float,
) {
    private val pages = mutableListOf<StringBuilder>()
    private var activePage = 0
    private val current: StringBuilder get() = pages[activePage]

    init {
        newPage()
    }

    val pageCount: Int get() = pages.size

    /** Appends a page and makes it the drawing target. */
    fun newPage() {
        pages.add(StringBuilder())
        activePage = pages.lastIndex
    }

    /**
     * Points subsequent drawing at an already-created page. Used to stamp "Page x of y" footers
     * once the total page count is finally known.
     */
    fun selectPage(index: Int) {
        activePage = index.coerceIn(0, pages.lastIndex)
    }

    /** Draws [text] with its left edge at [x] and its baseline at [y]. */
    fun text(
        text: String,
        x: Float,
        y: Float,
        size: Float,
        bold: Boolean = false,
        color: PdfColor = PdfColor.BLACK,
    ) {
        if (text.isEmpty()) return
        val font = if (bold) "F2" else "F1"
        current.append(color.fillOp())
            .append("BT /").append(font).append(' ').append(num(size)).append(" Tf ")
            .append(num(x)).append(' ').append(num(pageHeight - y)).append(" Td (")
            .append(PdfText.escape(text))
            .append(") Tj ET\n")
    }

    /** Draws [text] so that its right edge sits at [xRight] — used for amount columns. */
    fun textRight(
        text: String,
        xRight: Float,
        y: Float,
        size: Float,
        bold: Boolean = false,
        color: PdfColor = PdfColor.BLACK,
    ) = text(text, xRight - PdfText.width(text, size, bold), y, size, bold, color)

    /** Draws [text] horizontally centred on [xCenter]. */
    fun textCenter(
        text: String,
        xCenter: Float,
        y: Float,
        size: Float,
        bold: Boolean = false,
        color: PdfColor = PdfColor.BLACK,
    ) = text(text, xCenter - PdfText.width(text, size, bold) / 2f, y, size, bold, color)

    /** Filled rectangle whose top-left corner is ([x], [y]). */
    fun rect(x: Float, y: Float, width: Float, height: Float, color: PdfColor) {
        current.append(color.fillOp())
            .append(num(x)).append(' ').append(num(pageHeight - y - height)).append(' ')
            .append(num(width)).append(' ').append(num(height)).append(" re f\n")
    }

    /** Filled rectangle with rounded corners, approximated with Bezier curves. */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: PdfColor) {
        val r = minOf(radius, width / 2f, height / 2f)
        val k = r * 0.5523f // circle-to-Bezier magic constant
        val bottom = pageHeight - y - height
        val top = pageHeight - y
        val right = x + width
        current.append(color.fillOp())
            .append(num(x + r)).append(' ').append(num(bottom)).append(" m\n")
            .append(num(right - r)).append(' ').append(num(bottom)).append(" l\n")
            .curve(right - r + k, bottom, right, bottom + r - k, right, bottom + r)
            .append(num(right)).append(' ').append(num(top - r)).append(" l\n")
            .curve(right, top - r + k, right - r + k, top, right - r, top)
            .append(num(x + r)).append(' ').append(num(top)).append(" l\n")
            .curve(x + r - k, top, x, top - r + k, x, top - r)
            .append(num(x)).append(' ').append(num(bottom + r)).append(" l\n")
            .curve(x, bottom + r - k, x + r - k, bottom, x + r, bottom)
            .append("f\n")
    }

    fun line(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        width: Float = 0.5f,
        color: PdfColor = PdfColor.LINE,
    ) {
        current.append(color.strokeOp())
            .append(num(width)).append(" w ")
            .append(num(x1)).append(' ').append(num(pageHeight - y1)).append(" m ")
            .append(num(x2)).append(' ').append(num(pageHeight - y2)).append(" l S\n")
    }

    /** Horizontal rule across the full width between the given margins. */
    fun hLine(y: Float, from: Float, to: Float, width: Float = 0.5f, color: PdfColor = PdfColor.LINE) =
        line(from, y, to, y, width, color)

    /**
     * Serialises everything written so far into PDF bytes.
     *
     * Object numbering: 1 = catalog, 2 = page tree, 3/4 = the two fonts, then a page object and a
     * content-stream object per page. The cross-reference table needs each object's exact byte
     * offset, so the document is assembled as a single Latin-1 string (one char == one byte) and
     * converted at the end.
     */
    fun build(): ByteArray {
        val objectCount = 4 + pages.size * 2
        val offsets = IntArray(objectCount + 1)
        val out = StringBuilder()

        // A comment with high bytes marks the file as binary for transfer tools.
        out.append("%PDF-1.4\n%âãÏÓ\n")

        fun obj(number: Int, body: String) {
            offsets[number] = out.length
            out.append(number).append(" 0 obj\n").append(body).append("\nendobj\n")
        }

        val kids = pages.indices.joinToString(" ") { "${5 + it * 2} 0 R" }
        obj(1, "<< /Type /Catalog /Pages 2 0 R >>")
        obj(2, "<< /Type /Pages /Kids [$kids] /Count ${pages.size} >>")
        obj(3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>")
        obj(4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>")

        pages.forEachIndexed { index, content ->
            val pageNumber = 5 + index * 2
            val contentNumber = pageNumber + 1
            obj(
                pageNumber,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${num(pageWidth)} ${num(pageHeight)}] " +
                    "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents $contentNumber 0 R >>",
            )
            obj(contentNumber, "<< /Length ${content.length} >>\nstream\n$content\nendstream")
        }

        val xrefOffset = out.length
        out.append("xref\n0 ").append(objectCount + 1).append('\n')
        out.append("0000000000 65535 f \n") // entries are exactly 20 bytes each
        for (number in 1..objectCount) {
            out.append(offsets[number].toString().padStart(10, '0')).append(" 00000 n \n")
        }
        out.append("trailer\n<< /Size ").append(objectCount + 1).append(" /Root 1 0 R >>\n")
            .append("startxref\n").append(xrefOffset).append("\n%%EOF\n")

        return ByteArray(out.length) { out[it].code.toByte() }
    }

    private fun StringBuilder.curve(
        c1x: Float,
        c1y: Float,
        c2x: Float,
        c2y: Float,
        x: Float,
        y: Float,
    ): StringBuilder = append(num(c1x)).append(' ').append(num(c1y)).append(' ')
        .append(num(c2x)).append(' ').append(num(c2y)).append(' ')
        .append(num(x)).append(' ').append(num(y)).append(" c\n")

    private companion object {
        /** Locale-independent number formatting — `Float.toString()` can emit exponents. */
        fun num(value: Float): String {
            val scaled = round(abs(value) * 1000.0).toLong()
            val whole = scaled / 1000
            val fraction = (scaled % 1000).toString().padStart(3, '0').trimEnd('0')
            val sign = if (value < 0f && scaled != 0L) "-" else ""
            return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
        }
    }
}

/** An RGB colour with components in 0..1. */
internal data class PdfColor(val r: Float, val g: Float, val b: Float) {

    fun fillOp(): String = "${c(r)} ${c(g)} ${c(b)} rg\n"

    fun strokeOp(): String = "${c(r)} ${c(g)} ${c(b)} RG\n"

    private fun c(v: Float): String {
        val scaled = round(v.coerceIn(0f, 1f) * 1000.0).toLong()
        val fraction = (scaled % 1000).toString().padStart(3, '0').trimEnd('0')
        return if (fraction.isEmpty()) "${scaled / 1000}" else "${scaled / 1000}.$fraction"
    }

    companion object {
        fun of(r: Int, g: Int, b: Int) = PdfColor(r / 255f, g / 255f, b / 255f)

        val BLACK = of(0, 0, 0)
        val WHITE = of(255, 255, 255)
        val LINE = of(214, 217, 224)
    }
}

/** Text measurement and encoding for the base-14 Helvetica fonts. */
internal object PdfText {

    /** Advance widths (per 1000 units of font size) for ASCII 32..126. */
    private val REGULAR = intArrayOf(
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584,
    )

    private val BOLD = intArrayOf(
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584,
    )

    /** Width of [text] in points when set in Helvetica at [size]. */
    fun width(text: String, size: Float, bold: Boolean): Float {
        val table = if (bold) BOLD else REGULAR
        var total = 0
        for (char in text) {
            val code = char.code
            total += when {
                code in 32..126 -> table[code - 32]
                else -> table['n'.code - 32] // Latin-1 accents and substitutions: close enough
            }
        }
        return total * size / 1000f
    }

    /** Shortens [text] with an ellipsis so it fits inside [maxWidth]. */
    fun fit(text: String, maxWidth: Float, size: Float, bold: Boolean): String {
        if (width(text, size, bold) <= maxWidth) return text
        val ellipsis = "..."
        val budget = maxWidth - width(ellipsis, size, bold)
        if (budget <= 0f) return ""
        var end = text.length
        while (end > 0 && width(text.substring(0, end), size, bold) > budget) end--
        return text.substring(0, end).trimEnd() + ellipsis
    }

    /**
     * Maps a character to its `WinAnsiEncoding` byte. ASCII and Latin-1 pass through; anything
     * else (there is no glyph for it in the base-14 fonts) becomes `?`.
     */
    private fun toWinAnsi(char: Char): Char = when (char.code) {
        in 32..126, in 160..255 -> char
        else -> '?'
    }

    /** Escapes a string for a PDF literal `(...)` and forces it into WinAnsi. */
    fun escape(text: String): String {
        val sb = StringBuilder(text.length + 8)
        for (char in text) {
            when (val mapped = toWinAnsi(char)) {
                '(', ')', '\\' -> sb.append('\\').append(mapped)
                else -> sb.append(mapped)
            }
        }
        return sb.toString()
    }
}
