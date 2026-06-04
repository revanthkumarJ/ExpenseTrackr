package com.revanthdev.expensetrackr.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("isDarkMode")
        val APP_LOCK_TYPE = stringPreferencesKey("appLockType")
        val PIN_HASH = stringPreferencesKey("pinHash")
        val LOCK_TIMEOUT_MINUTES = intPreferencesKey("lockTimeoutMinutes")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("dailyReminderEnabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("dailyReminderHour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("dailyReminderMinute")
        val BUDGET_ALERT_ENABLED = booleanPreferencesKey("budgetAlertEnabled")
        val OVERALL_MONTHLY_BUDGET = doublePreferencesKey("overallMonthlyBudget")
        val IS_ONBOARDING_DONE = booleanPreferencesKey("isOnboardingDone")
    }

    override fun getSettings(): Flow<AppSettings> =
        dataStore.data.map { prefs -> prefs.toAppSettings() }

    override suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            if (settings.isDarkMode != null) {
                prefs[IS_DARK_MODE] = settings.isDarkMode!!
            } else {
                prefs.remove(IS_DARK_MODE)
            }
            prefs[APP_LOCK_TYPE] = settings.appLockType.name
            if (settings.pinHash != null) {
                prefs[PIN_HASH] = settings.pinHash!!
            } else {
                prefs.remove(PIN_HASH)
            }
            prefs[LOCK_TIMEOUT_MINUTES] = settings.lockTimeoutMinutes
            prefs[DAILY_REMINDER_ENABLED] = settings.dailyReminderEnabled
            prefs[DAILY_REMINDER_HOUR] = settings.dailyReminderHour
            prefs[DAILY_REMINDER_MINUTE] = settings.dailyReminderMinute
            prefs[BUDGET_ALERT_ENABLED] = settings.budgetAlertEnabled
            if (settings.overallMonthlyBudget != null) {
                prefs[OVERALL_MONTHLY_BUDGET] = settings.overallMonthlyBudget!!
            } else {
                prefs.remove(OVERALL_MONTHLY_BUDGET)
            }
            prefs[IS_ONBOARDING_DONE] = settings.isOnboardingDone
        }
    }

    override suspend fun getSettingsOnce(): AppSettings =
        dataStore.data.first().toAppSettings()

    private fun Preferences.toAppSettings(): AppSettings = AppSettings(
        isDarkMode = this[IS_DARK_MODE],
        appLockType = this[APP_LOCK_TYPE]?.let { name ->
            runCatching { AppLockType.valueOf(name) }.getOrDefault(AppLockType.NONE)
        } ?: AppLockType.NONE,
        pinHash = this[PIN_HASH],
        lockTimeoutMinutes = this[LOCK_TIMEOUT_MINUTES] ?: 1,
        dailyReminderEnabled = this[DAILY_REMINDER_ENABLED] ?: false,
        dailyReminderHour = this[DAILY_REMINDER_HOUR] ?: 21,
        dailyReminderMinute = this[DAILY_REMINDER_MINUTE] ?: 0,
        budgetAlertEnabled = this[BUDGET_ALERT_ENABLED] ?: true,
        overallMonthlyBudget = this[OVERALL_MONTHLY_BUDGET],
        isOnboardingDone = this[IS_ONBOARDING_DONE] ?: false
    )
}
