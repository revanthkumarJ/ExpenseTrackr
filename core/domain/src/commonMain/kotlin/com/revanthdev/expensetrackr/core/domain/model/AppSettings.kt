package com.revanthdev.expensetrackr.core.domain.model

data class AppSettings(
    val isDarkMode: Boolean? = null,
    val appLockType: AppLockType = AppLockType.NONE,
    val pinHash: String? = null,
    val lockTimeoutMinutes: Int = 1,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 21,
    val dailyReminderMinute: Int = 0,
    val budgetAlertEnabled: Boolean = true,
    val overallMonthlyBudget: Double? = null,
    val isOnboardingDone: Boolean = false
)
