package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.CategoryCard
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetYellow
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toMonthYear
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object DashboardRoute

@Serializable
data class SubCategoryRoute(val categoryId: Long, val categoryName: String)

data class CategoryUi(
    val category: Category,
    val total: Double,
    val percentage: Double,
    val budgetProgress: Float?
)

data class DashboardState(
    val monthLabel: String = "",
    val totalSpend: Double = 0.0,
    val overallBudget: Double? = null,
    val overallProgress: Float? = null,
    val categories: List<CategoryUi> = emptyList(),
    val filter: DateFilter = DateFilter.ThisMonth,
    val isLoading: Boolean = true
)

sealed interface DashboardAction {
    data class OnFilterChange(val filter: DateFilter) : DashboardAction
    data class OnCategoryClick(val categoryId: Long, val categoryName: String) : DashboardAction
    data object OnAddExpenseClick : DashboardAction
}

sealed interface DashboardEvent {
    data class NavigateToSubCategory(val categoryId: Long, val categoryName: String) : DashboardEvent
    data object NavigateToAddExpense : DashboardEvent
}

class DashboardViewModel(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadData(DateFilter.ThisMonth)
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnFilterChange -> loadData(action.filter)
            is DashboardAction.OnCategoryClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToSubCategory(action.categoryId, action.categoryName))
            }
            DashboardAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToAddExpense)
            }
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
                val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val monthLabel = now.toMonthYear()
                val overallBudget = settings.overallMonthlyBudget
                val categoryUis = categories.map { cat ->
                    val catTotal = spendMap[cat.id] ?: 0.0
                    val percent = if (total > 0) (catTotal / total) * 100 else 0.0
                    val budgetProgress = cat.budgetAmount?.let { budget ->
                        if (budget > 0) (catTotal / budget).toFloat() else null
                    }
                    CategoryUi(cat, catTotal, percent, budgetProgress)
                }.filter { it.total > 0 }

                DashboardState(
                    monthLabel = monthLabel,
                    totalSpend = total,
                    overallBudget = overallBudget,
                    overallProgress = overallBudget?.let { if (it > 0) (total / it).toFloat() else null },
                    categories = categoryUis,
                    filter = filter,
                    isLoading = false
                )
            }.collect { newState -> _state.value = newState }
        }
    }
}

@Composable
fun DashboardRoot(
    onNavigateToSubCategory: (Long, String) -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DashboardEvent.NavigateToSubCategory -> onNavigateToSubCategory(event.categoryId, event.categoryName)
            DashboardEvent.NavigateToAddExpense -> onNavigateToAddExpense()
        }
    }
    val state by viewModel.state.collectAsState()
    DashboardScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.monthLabel, style = MaterialTheme.typography.titleMedium)
                        Text(state.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.FilterList, "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(DashboardAction.OnAddExpenseClick) }) {
                Icon(Icons.Rounded.Add, "Add Expense")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(DashboardAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.overallBudget != null && state.overallProgress != null) {
                BudgetProgressBar(
                    spent = state.totalSpend.toCurrencyString(),
                    budget = state.overallBudget.toCurrencyString(),
                    progress = state.overallProgress,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.categories.isEmpty()) {
                EmptyState(
                    title = "No expenses yet",
                    message = "Tap the + button to add your first expense",
                    actionLabel = "Add Expense",
                    onAction = { onAction(DashboardAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories, key = { it.category.id }) { catUi ->
                        CategoryCard(
                            name = catUi.category.name,
                            icon = catUi.category.icon,
                            color = hexToColor(catUi.category.colorHex),
                            totalAmount = catUi.total.toCurrencyString(),
                            percentage = "${catUi.percentage}%",
                            budgetAmount = catUi.category.budgetAmount?.toCurrencyString(),
                            budgetProgress = catUi.budgetProgress,
                            onClick = { onAction(DashboardAction.OnCategoryClick(catUi.category.id, catUi.category.name)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressBar(spent: String, budget: String, progress: Float, modifier: Modifier = Modifier) {
    val color = when {
        progress < 0.7f -> BudgetGreen
        progress < 0.9f -> BudgetYellow
        else -> BudgetRed
    }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Monthly Budget", style = MaterialTheme.typography.titleSmall)
                Text("$spent / $budget", style = MaterialTheme.typography.titleSmall, color = color)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

val dashboardModule = org.koin.dsl.module {
    viewModelOf(::DashboardViewModel)
}
