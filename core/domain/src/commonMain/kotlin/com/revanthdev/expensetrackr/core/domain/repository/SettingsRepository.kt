package com.revanthdev.expensetrackr.core.domain.repository

import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
    suspend fun getSettingsOnce(): AppSettings
}
