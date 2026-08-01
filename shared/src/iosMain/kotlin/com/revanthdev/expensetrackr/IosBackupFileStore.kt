package com.revanthdev.expensetrackr

import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import com.revanthdev.expensetrackr.core.domain.repository.FileOpenResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController

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

    override suspend fun writeBytes(fileName: String, bytes: ByteArray, mimeType: String): Boolean =
        withContext(Dispatchers.Default) {
            runCatching {
                val path = folderPath() + "/" + fileName
                bytes.toNSData().writeToFile(path, true)
            }.getOrDefault(false)
        }

    /**
     * Shows the system "Open in…" menu for the file. iOS has no single default handler per type,
     * so the menu is the equivalent of Android's viewer chooser; it returns false when no
     * installed app claims the type, which maps to [FileOpenResult.NoAppFound].
     *
     * The controller has to be held onto — it is deallocated (and the menu vanishes) if the only
     * reference is a local.
     */
    override suspend fun openFile(fileName: String, mimeType: String): FileOpenResult =
        withContext(Dispatchers.Main) {
            runCatching {
                val path = folderPath() + "/" + fileName
                if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
                    return@runCatching FileOpenResult.Failed
                }
                val view = UIApplication.sharedApplication.keyWindow?.rootViewController?.view
                    ?: return@runCatching FileOpenResult.Failed

                val controller =
                    UIDocumentInteractionController.interactionControllerWithURL(
                        NSURL.fileURLWithPath(path),
                    )
                openInMenuController = controller
                val presented = controller.presentOpenInMenuFromRect(
                    rect = view.bounds,
                    inView = view,
                    animated = true,
                )
                if (presented) FileOpenResult.Opened else FileOpenResult.NoAppFound
            }.getOrDefault(FileOpenResult.Failed)
        }

    /** Strong reference for the presented "Open in…" menu; see [openFile]. */
    private var openInMenuController: UIDocumentInteractionController? = null

    /** Copies the Kotlin array into an [NSData]; pinning an empty array is not allowed. */
    private fun ByteArray.toNSData(): NSData =
        if (isEmpty()) {
            NSData()
        } else {
            usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }
        }
}
