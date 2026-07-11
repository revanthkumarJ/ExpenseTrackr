package com.revanthdev.expensetrackr

import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/**
 * iOS backup store. Writes CSVs to an "ExpenseTrackr" folder in the app's Documents directory
 * (visible in the Files app and preserved across app launches). iOS has no "clear app data"
 * concept like Android, so Documents is the natural home.
 */
@OptIn(ExperimentalForeignApi::class)
class IosBackupFileStore : BackupFileStore {

    override val locationLabel: String = "Documents/ExpenseTrackr"

    private fun folderPath(): String {
        val docs: NSURL = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )!!
        val dir = docs.path!! + "/ExpenseTrackr"
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        return dir
    }

    override suspend fun writeText(fileName: String, content: String): Boolean =
        withContext(Dispatchers.Default) {
            runCatching {
                val path = folderPath() + "/" + fileName
                (content as NSString).writeToFile(path, true, NSUTF8StringEncoding, null)
            }.getOrDefault(false)
        }

    override suspend fun readText(fileName: String): String? =
        withContext(Dispatchers.Default) {
            runCatching {
                val path = folderPath() + "/" + fileName
                NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            }.getOrNull()
        }
}
