package com.revanthdev.expensetrackr.core.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "expense_trackr_settings"
)

actual fun createSettingsDataStore(context: Any?): DataStore<Preferences> {
    val appContext = context as Context
    return appContext.dataStore
}
