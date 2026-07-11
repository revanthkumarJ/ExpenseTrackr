package com.revanthdev.expensetrackr.feature.settings.presentation.sync

import kotlinx.datetime.LocalDateTime

/**
 * One row of a backup CSV. Categories are stored as text (name + icon + color) so a backup is
 * self-sufficient and can be restored even after the app's categories were cleared. [id] is the
 * Room primary key — it is the stable identity used to update/dedupe on re-sync and restore.
 */
internal data class BackupRow(
    val id: Long,
    val name: String,
    val amount: Double,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val subCategoryName: String?,
    val notes: String?,
    val date: LocalDateTime,
    val createdAt: LocalDateTime,
)

/** Encodes/decodes [BackupRow]s to CSV text (RFC-4180-style quoting). Human-readable in Excel. */
internal object TransactionCsv {

    private val HEADER = listOf(
        "id", "name", "amount", "category", "categoryIcon", "categoryColor",
        "subCategory", "notes", "date", "createdAt",
    )

    fun encode(rows: List<BackupRow>): String {
        val sb = StringBuilder()
        sb.append(HEADER.joinToString(",") { escape(it) }).append('\n')
        for (r in rows) {
            val cells = listOf(
                r.id.toString(),
                r.name,
                r.amount.toString(),
                r.categoryName,
                r.categoryIcon,
                r.categoryColor,
                r.subCategoryName.orEmpty(),
                r.notes.orEmpty(),
                r.date.toString(),
                r.createdAt.toString(),
            )
            sb.append(cells.joinToString(",") { escape(it) }).append('\n')
        }
        return sb.toString()
    }

    /** Parses CSV text into rows, skipping the header and any malformed lines. */
    fun decode(csv: String): List<BackupRow> {
        val records = parse(csv)
        if (records.isEmpty()) return emptyList()
        val body = if (records.first().firstOrNull()?.trim()?.lowercase() == "id") records.drop(1) else records
        return body.mapNotNull { cols ->
            runCatching {
                BackupRow(
                    id = cols[0].trim().toLong(),
                    name = cols[1],
                    amount = cols[2].trim().toDouble(),
                    categoryName = cols.getOrElse(3) { "" },
                    categoryIcon = cols.getOrElse(4) { "" },
                    categoryColor = cols.getOrElse(5) { "" },
                    subCategoryName = cols.getOrNull(6)?.ifBlank { null },
                    notes = cols.getOrNull(7)?.ifBlank { null },
                    date = LocalDateTime.parse(cols[8].trim()),
                    createdAt = LocalDateTime.parse(cols[9].trim()),
                )
            }.getOrNull()
        }
    }

    private fun escape(value: String): String {
        val needsQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }

    /** Minimal CSV parser: handles quoted fields, escaped quotes ("") and newlines inside quotes. */
    private fun parse(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var record = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> { record.add(field.toString()); field.clear() }
                c == '\n' -> { record.add(field.toString()); records.add(record); record = mutableListOf(); field.clear() }
                c == '\r' -> Unit // handled as part of \r\n
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || record.isNotEmpty()) {
            record.add(field.toString())
            records.add(record)
        }
        // Drop blank trailing lines.
        return records.filter { row -> row.any { it.isNotEmpty() } }
    }
}
