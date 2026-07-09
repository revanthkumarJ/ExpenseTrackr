package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.ui.graphics.Color
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter

data class CategoryStat(
    val category: Category,
    val total: Double,
    val percentage: Double,
    val color: Color
)

/** One bar in the "spending over time" chart. [label] is a short period label (Mon, W1, Jan…). */
data class TimeBucket(
    val label: String,
    val total: Double
)

data class AnalyticsState(
    val filter: DateFilter = DateFilter.ThisMonth,
    val totalSpend: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val spendByTime: List<TimeBucket> = emptyList(),
    val highestCategory: String = "",
    val avgDailySpend: String = "",
    // Income recorded in the selected period, and savings (income - spend).
    // income == 0.0 means no income recorded → the savings section is hidden.
    val income: Double = 0.0,
    val saved: Double = 0.0,
    val isLoading: Boolean = true
) {
    val savingsRate: Float get() = if (income > 0) (saved / income).toFloat() else 0f
}

sealed interface AnalyticsAction {
    data class OnFilterChange(val filter: DateFilter) : AnalyticsAction
}
