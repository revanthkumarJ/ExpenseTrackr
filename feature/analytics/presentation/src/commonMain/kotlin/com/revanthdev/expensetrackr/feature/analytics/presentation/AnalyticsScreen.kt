package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.theme.categoryColorPalette
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
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
                AnalyticsState(
                    filter = filter,
                    totalSpend = total,
                    categoryStats = stats,
                    highestCategory = highest,
                    avgDailySpend = "-",
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
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DateFilterRow(selected = state.filter, onSelect = { onAction(AnalyticsAction.OnFilterChange(it)) })
                }
                item {
                    Text(
                        "Total: ${state.totalSpend.toCurrencyString()}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                if (state.categoryStats.isNotEmpty()) {
                    item { PieChart(state.categoryStats, modifier = Modifier.fillMaxWidth().height(240.dp)) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Summary", style = MaterialTheme.typography.titleMedium)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Highest Category", style = MaterialTheme.typography.bodyMedium)
                                    Text(state.highestCategory, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    items(state.categoryStats) { stat ->
                        LegendItem(stat)
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(stats: List<CategoryStat>, modifier: Modifier = Modifier) {
    val total = stats.sumOf { it.total }.toFloat()
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val diameter = size.minDimension * 0.85f
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        stats.forEach { stat ->
            val sweep = if (total > 0) ((stat.total / total) * 360f).toFloat() else 0f
            drawArc(
                color = stat.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = Size(diameter, diameter)
            )
            startAngle += sweep
        }
    }
}

@Composable
fun LegendItem(stat: CategoryStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = MaterialTheme.shapes.extraSmall, color = stat.color, modifier = Modifier.size(12.dp)) {}
        Text("${stat.category.icon} ${stat.category.name}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(stat.total.toCurrencyString(), style = MaterialTheme.typography.bodyMedium)
        Text("$stat.percentage}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

val analyticsModule = org.koin.dsl.module {
    viewModelOf(::AnalyticsViewModel)
}
