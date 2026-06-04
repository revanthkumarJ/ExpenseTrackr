package com.revanthdev.expensetrackr.core.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createSettingsDataStore(context: Any?): DataStore<Preferences>
