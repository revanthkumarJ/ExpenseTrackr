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
    // When true (default) the user may record expenses that exceed their monthly/category budget.
    // When false, adding or editing an expense that would overflow a budget is blocked with a warning.
    val allowExceedBudget: Boolean = true,
    val isOnboardingDone: Boolean = false
)
