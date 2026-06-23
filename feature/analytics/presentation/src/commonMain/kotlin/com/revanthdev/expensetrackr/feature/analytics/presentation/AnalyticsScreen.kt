package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.categoryColorPalette
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.SalaryCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import expensetrackr.core.presentation.generated.resources.*
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object AnalyticsRoute

data class CategoryStat(
    val category: Category,
    val total: Double,
    val percentage: Double,
    val color: Color
)

data class AnalyticsState(
    val filter: DateFilter = DateFilter.ThisMonth,
    val totalSpend: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val highestCategory: String = "",
    val avgDailySpend: String = "",
    // Income (salary) applicable to the selected period, and savings (income - spend).
    // income == 0.0 means no salary is set → the savings section is hidden.
    val income: Double = 0.0,
    val saved: Double = 0.0,
    val isLoading: Boolean = true
) {
    val savingsRate: Float get() = if (income > 0) (saved / income).toFloat() else 0f
}

sealed interface AnalyticsAction {
    data class OnFilterChange(val filter: DateFilter) : AnalyticsAction
}

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
                settingsRepository.getSettings()
            ) { categories, spendMap, total, settings ->
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
                val days = when (filter) {
                    DateFilter.ThisWeek -> 7
                    DateFilter.ThisMonth, DateFilter.LastMonth -> 30
                    DateFilter.ThisYear -> 365
                    is DateFilter.CustomRange -> maxOf(1, filter.start.daysUntil(filter.end) + 1)
                }
                val avg = if (total > 0 && days > 0) (total / days).toCurrencyString() else "-"
                val (start, end) = filter.toLocalDateRange()
                val income = SalaryCalculator.salaryForRange(settings.salaryHistory, start, end)
                AnalyticsState(
                    filter = filter,
                    totalSpend = total,
                    categoryStats = stats,
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

@Composable
fun AnalyticsRoot(viewModel: AnalyticsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    AnalyticsScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(state: AnalyticsState, onAction: (AnalyticsAction) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.nav_analytics)) }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Filter chips are always visible so the user can switch periods even when a
            // selected period happens to have no data.
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(AnalyticsAction.OnFilterChange(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                state.isLoading -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.categoryStats.isEmpty() -> EmptyState(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.analytics_empty_title),
                    message = stringResource(Res.string.analytics_empty_message),
                    emoji = "📊"
                )

                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DonutChart(
                            stats = state.categoryStats,
                            centerLabel = stringResource(Res.string.common_total),
                            centerValue = state.totalSpend.toCurrencyString(),
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(stringResource(Res.string.analytics_top_category), state.highestCategory, Modifier.weight(1f))
                            StatCard(stringResource(Res.string.analytics_avg_day), state.avgDailySpend, Modifier.weight(1f))
                        }
                    }
                    if (state.income > 0) {
                        item {
                            Text(
                                stringResource(Res.string.analytics_spent_vs_saved),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(stringResource(Res.string.analytics_income), state.income.toCurrencyString(), Modifier.weight(1f))
                                StatCard(
                                    stringResource(Res.string.analytics_saved),
                                    state.saved.toCurrencyString(),
                                    Modifier.weight(1f)
                                )
                                StatCard(
                                    stringResource(Res.string.analytics_savings_rate),
                                    state.savingsRate.coerceAtLeast(0f).let { "${(it * 100).toInt()}%" },
                                    Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            SpentVsSavedChart(
                                spent = state.totalSpend,
                                saved = state.saved,
                                income = state.income,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Text(stringResource(Res.string.analytics_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    items(state.categoryStats, key = { it.category.id }) { stat ->
                        LegendItem(stat, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun DonutChart(
    stats: List<CategoryStat>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val total = stats.sumOf { it.total }.toFloat()
    val sweepProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "donutSweep"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.16f
            val diameter = size.minDimension * 0.82f - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var startAngle = -90f
            stats.forEach { stat ->
                val full = if (total > 0) ((stat.total / total) * 360f).toFloat() else 0f
                val sweep = full * sweepProgress
                drawArc(
                    color = stat.color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 3f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                startAngle += full
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(centerValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LegendItem(stat: CategoryStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(4.dp), color = stat.color, modifier = Modifier.size(14.dp)) {}
            Text("${stat.category.icon} ${stat.category.name}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(stat.total.toCurrencyString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Surface(shape = RoundedCornerShape(6.dp), color = stat.color.copy(alpha = 0.14f)) {
                Text(
                    stat.percentage.toPercentString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = stat.color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Two horizontal bars comparing spend vs savings, each scaled against income so their
 * lengths are directly comparable. When overspent, the saved bar is empty and shown in red.
 */
@Composable
fun SpentVsSavedChart(spent: Double, saved: Double, income: Double, modifier: Modifier = Modifier) {
    val denominator = maxOf(income, spent, 1.0)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BarRow(
                label = stringResource(Res.string.analytics_spent),
                value = spent.toCurrencyString(),
                fraction = (spent / denominator).toFloat().coerceIn(0f, 1f),
                color = BudgetRed
            )
            BarRow(
                label = stringResource(Res.string.analytics_saved),
                value = saved.toCurrencyString(),
                fraction = (saved / denominator).toFloat().coerceIn(0f, 1f),
                color = if (saved < 0) BudgetRed else BudgetGreen
            )
        }
    }
}

@Composable
private fun BarRow(label: String, value: String, fraction: Float, color: Color) {
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(700), label = "barFill")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
        }
        Box(
            Modifier.fillMaxWidth().height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier.fillMaxWidth(animated).fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

val analyticsModule = org.koin.dsl.module {
    viewModelOf(::AnalyticsViewModel)
}
