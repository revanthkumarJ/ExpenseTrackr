package com.revanthdev.expensetrackr.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import java.io.File

actual fun createSettingsDataStore(context: Any?): DataStore<Preferences> {
    val storageDir = File(System.getProperty("user.home"), ".expensetrackr")
    storageDir.mkdirs()
    val storePath = File(storageDir, "expense_trackr_settings.preferences_pb").absolutePath
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { storePath.toPath() }
    )
}
