package com.revanthdev.expensetrackr.core.domain.repository

/**
 * Platform bridge for reading/writing backup files in a user-visible location that SURVIVES the
 * app's data being cleared (e.g. a "ExpenseTrackr" folder in shared storage). The Android
 * implementation writes to Downloads/ExpenseTrackr via MediaStore.
 *
 * Files are addressed by a simple [fileName] (e.g. "expenses.csv"); the platform decides the
 * containing folder. Writing overwrites any existing file with the same name.
 */
interface BackupFileStore {
    /** Human-readable location shown to the user, e.g. "Downloads/ExpenseTrackr". */
    val locationLabel: String

    /** Writes [content] to the backup folder as [fileName], overwriting. Returns true on success. */
    suspend fun writeText(fileName: String, content: String): Boolean

    /** Reads [fileName] from the backup folder, or null if it is missing / unreadable. */
    suspend fun readText(fileName: String): String?
}
