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

    /**
     * Writes raw [bytes] to the same folder as [fileName], overwriting. [mimeType] lets the
     * platform register the file with the right type (e.g. "application/pdf") so the system file
     * browser can open it. Used for the generated PDF/Excel reports.
     */
    suspend fun writeBytes(fileName: String, bytes: ByteArray, mimeType: String): Boolean

    /**
     * Hands [fileName] to whichever app on the device can display [mimeType].
     *
     * Implementations must never throw: a device with no PDF or spreadsheet viewer installed is
     * normal, and the caller is expected to surface [FileOpenResult.NoAppFound] as a message
     * rather than treat it as an error.
     */
    suspend fun openFile(fileName: String, mimeType: String): FileOpenResult
}

/** Outcome of asking the platform to open a saved file. */
enum class FileOpenResult {
    /** A viewer app was launched. */
    Opened,

    /** Nothing on the device can handle this file type. */
    NoAppFound,

    /** The file was missing, or the platform refused the request for some other reason. */
    Failed,
}
