package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.theme.categoryColorPalette
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.SalaryCalculator
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class AnalyticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init { loadData(DateFilter.ThisMonth) }

    fun onAction(action: AnalyticsAction) {
        when (action) {
            is AnalyticsAction.OnFilterChange -> loadData(action.filter)
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            combine(
                categoryRepository.getAllCategories(),
                expenseRepository.getSpendByCategory(filter),
                expenseRepository.getTotalSpend(filter),
                settingsRepository.getSettings(),
                expenseRepository.getExpensesWithDetails(filter)
            ) { categories, spendMap, total, settings, expenses ->
                val stats = categories.mapIndexed { idx, cat ->
                    val t = spendMap[cat.id] ?: 0.0
                    CategoryStat(
                        category = cat,
                        total = t,
                        percentage = if (total > 0) (t / total) * 100 else 0.0,
                        color = try { hexToColor(cat.colorHex) } catch (e: Exception) { categoryColorPalette[idx % categoryColorPalette.size] }
                    )
                }.filter { it.total > 0 }.sortedByDescending { it.total }

                val highest = stats.firstOrNull()?.category?.name ?: "-"
                val (start, end) = filter.toLocalDateRange()
                // Average over the days actually elapsed in the period, never counting future days.
                // So on the 4th of the month the divisor is 4 (not 30); a past period (e.g. Last
                // Month) uses its full length since its end is already behind today.
                val today = kotlin.time.Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val elapsedDays = maxOf(1, start.daysUntil(minOf(end, today)) + 1)
                val avg = if (total > 0) (total / elapsedDays).toCurrencyString() else "-"
                val income = SalaryCalculator.salaryForRange(settings.salaryHistory, start, end)
                AnalyticsState(
                    filter = filter,
                    totalSpend = total,
                    categoryStats = stats,
                    spendByTime = bucketByTime(expenses),
                    highestCategory = highest,
                    avgDailySpend = avg,
                    income = income,
                    saved = income - total,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }
}

/** Inclusive LocalDate range for a filter, mirroring DateFilterHelper (which works in epoch ms). */
private fun DateFilter.toLocalDateRange(): Pair<LocalDate, LocalDate> {
    val today = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (this) {
        DateFilter.ThisMonth -> {
            val start = LocalDate(today.year, today.month, 1)
            start to start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        }
        DateFilter.LastMonth -> {
            val lastMonth = today.minus(1, DateTimeUnit.MONTH)
            val start = LocalDate(lastMonth.year, lastMonth.month, 1)
            start to start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        }
        DateFilter.ThisWeek -> {
            val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            monday to monday.plus(6, DateTimeUnit.DAY)
        }
        DateFilter.ThisYear -> LocalDate(today.year, 1, 1) to LocalDate(today.year, 12, 31)
        is DateFilter.CustomRange -> start to end
    }
}

private val DAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

/**
 * Sums [expenses] by day of the week (Sunday-first) across the whole selected period, so the chart
 * shows which weekday you spend the most on. All 7 days are always present (0.0 when none) so the
 * axis stays continuous regardless of which period is selected.
 */
private fun bucketByTime(expenses: List<ExpenseWithDetails>): List<TimeBucket> {
    val totals = DoubleArray(7)
    // kotlinx-datetime DayOfWeek.ordinal is Monday=0..Sunday=6; shift so Sunday=0 (Sunday-first).
    expenses.forEach { totals[(it.expenseDate.dayOfWeek.ordinal + 1) % 7] += it.amount }
    return DAY_LABELS.mapIndexed { i, label -> TimeBucket(label, totals[i]) }
}
