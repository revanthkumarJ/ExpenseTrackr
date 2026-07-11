package com.revanthdev.expensetrackr

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Writes backup files to a folder named "ExpenseTrackr" inside the device's shared Downloads,
 * so they survive the app's data being cleared or the app being uninstalled/reinstalled.
 *
 * On Android 10+ (API 29) this uses MediaStore (no storage permission needed). On older versions
 * it writes directly to the public Downloads folder (needs WRITE_EXTERNAL_STORAGE, declared in the
 * manifest with maxSdkVersion=28).
 */
private const val FOLDER = "ExpenseTrackr"
private const val MIME = "text/csv"

class AndroidBackupFileStore(private val context: Context) : BackupFileStore {

    override val locationLabel: String = "Downloads/$FOLDER"

    override suspend fun writeText(fileName: String, content: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    writeViaMediaStore(fileName, content)
                } else {
                    writeLegacy(fileName, content)
                }
            }.getOrDefault(false)
        }

    override suspend fun readText(fileName: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    readViaMediaStore(fileName)
                } else {
                    readLegacy(fileName)
                }
            }.getOrNull()
        }

    // ---- API 29+ : MediaStore ----

    private val downloadsUri: Uri
        get() = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

    private val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER"

    private fun writeViaMediaStore(fileName: String, content: String): Boolean {
        val resolver = context.contentResolver
        // Reuse an existing file (overwrite) or create a new one.
        val uri = findExisting(fileName) ?: resolver.insert(
            downloadsUri,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, MIME)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            },
        ) ?: return false
        // "wt" truncates any previous content before writing.
        resolver.openOutputStream(uri, "wt")?.use { it.write(content.toByteArray()) } ?: return false
        return true
    }

    private fun readViaMediaStore(fileName: String): String? {
        val uri = findExisting(fileName) ?: return null
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
    }

    private fun findExisting(fileName: String): Uri? {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection =
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val args = arrayOf(fileName, "$relativePath%")
        context.contentResolver.query(downloadsUri, projection, selection, args, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(downloadsUri, id.toString())
            }
        }
        return null
    }

    // ---- API < 29 : legacy public Downloads (needs WRITE_EXTERNAL_STORAGE) ----

    @Suppress("DEPRECATION")
    private fun legacyDir(): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FOLDER)

    private fun writeLegacy(fileName: String, content: String): Boolean {
        val dir = legacyDir()
        if (!dir.exists() && !dir.mkdirs()) return false
        File(dir, fileName).writeText(content)
        return true
    }

    private fun readLegacy(fileName: String): String? =
        File(legacyDir(), fileName).takeIf { it.exists() }?.readText()
}
