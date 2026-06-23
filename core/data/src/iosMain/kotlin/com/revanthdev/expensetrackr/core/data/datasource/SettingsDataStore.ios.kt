package com.revanthdev.expensetrackr.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createSettingsDataStore(context: Any?): DataStore<Preferences> {
    val docsDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )!!
    val storePath = docsDirectory.path + "/expense_trackr_settings.preferences_pb"
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { storePath.toPath() }
    )
}
