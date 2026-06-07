package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.revanthdev.expensetrackr.core.designsystem.theme.categoryColorPalette
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import kotlinx.datetime.daysUntil
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
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
    val isLoading: Boolean = true
)

sealed interface AnalyticsAction {
    data class OnFilterChange(val filter: DateFilter) : AnalyticsAction
}

class AnalyticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
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
                expenseRepository.getTotalSpend(filter)
            ) { categories, spendMap, total ->
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
                AnalyticsState(
                    filter = filter,
                    totalSpend = total,
                    categoryStats = stats,
                    highestCategory = highest,
                    avgDailySpend = avg,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
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
    Scaffold(topBar = { TopAppBar(title = { Text("Analytics") }) }) { padding ->
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
                    title = "No data for this period",
                    message = "Try a different period above, or add some expenses",
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
                            centerLabel = "Total",
                            centerValue = state.totalSpend.toCurrencyString(),
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Top Category", state.highestCategory, Modifier.weight(1f))
                            StatCard("Avg / Day", state.avgDailySpend, Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                    "${stat.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = stat.color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

val analyticsModule = org.koin.dsl.module {
    viewModelOf(::AnalyticsViewModel)
}
