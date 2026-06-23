package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.AnimatedProgressBar
import com.revanthdev.expensetrackr.core.designsystem.component.CategoryCard
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.component.ExpenseItemCard
import com.revanthdev.expensetrackr.core.designsystem.component.bounceClick
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetYellow
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.SalaryCalculator
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayTime
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
    // Salary for the current month (only populated when viewing ThisMonth). null = no salary set.
    val monthlySalary: Double? = null,
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
                // Budgets are monthly, so they only make sense while viewing the current month.
                // For any other period (this week, last month, this year, custom) we hide them.
                val isThisMonth = filter == DateFilter.ThisMonth
                val periodLabel = when (filter) {
                    DateFilter.ThisMonth -> now.toMonthYear()
                    DateFilter.ThisWeek -> "This Week"
                    DateFilter.LastMonth -> "Last Month"
                    DateFilter.ThisYear -> now.year.toString()
                    is DateFilter.CustomRange -> "Custom Range"
                }
                val overallBudget = if (isThisMonth) settings.overallMonthlyBudget else null
                val monthlySalary = if (isThisMonth) {
                    SalaryCalculator.salaryForMonth(
                        settings.salaryHistory,
                        SalaryCalculator.monthIndexOf(now.year, now.monthNumber)
                    ).takeIf { it > 0.0 }
                } else null
                val categoryUis = categories.map { cat ->
                    val catTotal = spendMap[cat.id] ?: 0.0
                    val percent = if (total > 0) (catTotal / total) * 100 else 0.0
                    val budgetProgress = if (isThisMonth) {
                        cat.budgetAmount?.let { budget ->
                            if (budget > 0) (catTotal / budget).toFloat() else null
                        }
                    } else null
                    CategoryUi(cat, catTotal, percent, budgetProgress)
                }.filter { it.total > 0 }.sortedByDescending { it.total }

                DashboardState(
                    monthLabel = periodLabel,
                    totalSpend = total,
                    overallBudget = overallBudget,
                    overallProgress = overallBudget?.let { if (it > 0) (total / it).toFloat() else null },
                    monthlySalary = monthlySalary,
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
                    val periodLabel = when (state.filter) {
                        DateFilter.ThisWeek -> stringResource(Res.string.filter_this_week)
                        DateFilter.LastMonth -> stringResource(Res.string.filter_last_month)
                        is DateFilter.CustomRange -> stringResource(Res.string.period_custom_range)
                        else -> state.monthLabel
                    }
                    Column {
                        Text(periodLabel, style = MaterialTheme.typography.titleMedium)
                        Text(state.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(DashboardAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add_expense)) },
                text = { Text(stringResource(Res.string.action_add)) }
            )
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

            if (state.monthlySalary != null) {
                SalaryRemainingCard(
                    salary = state.monthlySalary,
                    spent = state.totalSpend,
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
                    title = stringResource(Res.string.dashboard_empty_title),
                    message = stringResource(Res.string.dashboard_empty_message),
                    actionLabel = stringResource(Res.string.action_add_expense),
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
                            percentage = catUi.percentage.toPercentString(),
                            budgetAmount = catUi.category.budgetAmount?.toCurrencyString(),
                            budgetProgress = catUi.budgetProgress,
                            onClick = { onAction(DashboardAction.OnCategoryClick(catUi.category.id, catUi.category.name)) },
                            modifier = Modifier.animateItem()
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.dashboard_budget_monthly), style = MaterialTheme.typography.titleSmall)
                Text("$spent / $budget", style = MaterialTheme.typography.titleSmall, color = color)
            }
            Spacer(Modifier.height(10.dp))
            AnimatedProgressBar(progress = progress, color = color, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SalaryRemainingCard(salary: Double, spent: Double, modifier: Modifier = Modifier) {
    val remaining = salary - spent
    val progress = if (salary > 0) (spent / salary).toFloat() else 0f
    val overspent = remaining < 0
    val accent = when {
        overspent -> BudgetRed
        progress < 0.7f -> BudgetGreen
        progress < 0.9f -> BudgetYellow
        else -> BudgetRed
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(if (overspent) Res.string.dashboard_salary_overspent else Res.string.dashboard_salary_remaining),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(remaining.toCurrencyString(), style = MaterialTheme.typography.titleSmall, color = accent)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.dashboard_salary_of, spent.toCurrencyString(), salary.toCurrencyString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            AnimatedProgressBar(progress = progress.coerceIn(0f, 1f), color = accent, modifier = Modifier.fillMaxWidth())
        }
    }
}

val dashboardModule = org.koin.dsl.module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::SubCategoryDrilldownViewModel)
    viewModelOf(::FilteredExpensesViewModel)
}

// ---- Sub-Category Drilldown ----

@Serializable
data class FilteredExpensesRoute(val categoryId: Long, val subCategoryId: Long = -1L, val title: String = "")

data class SubCategoryItem(
    val subCategoryId: Long?,
    val name: String,
    val total: Double,
    val percentage: Double
)

data class SubCategoryDrilldownState(
    val categoryName: String = "",
    val filter: DateFilter = DateFilter.ThisMonth,
    val totalSpend: Double = 0.0,
    val items: List<SubCategoryItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface SubCategoryDrilldownAction {
    data class OnFilterChange(val filter: DateFilter) : SubCategoryDrilldownAction
    data class OnItemClick(val subCategoryId: Long?) : SubCategoryDrilldownAction
    data object OnAddExpenseClick : SubCategoryDrilldownAction
    data object OnBack : SubCategoryDrilldownAction
}

sealed interface SubCategoryDrilldownEvent {
    data class NavigateToFilteredExpenses(val categoryId: Long, val subCategoryId: Long, val title: String) : SubCategoryDrilldownEvent
    data object NavigateToAddExpense : SubCategoryDrilldownEvent
    data object NavigateBack : SubCategoryDrilldownEvent
}

class SubCategoryDrilldownViewModel(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val categoryId: Long = savedStateHandle["categoryId"] ?: 0L
    private val categoryName: String = savedStateHandle["categoryName"] ?: ""

    private val _state = MutableStateFlow(SubCategoryDrilldownState(categoryName = categoryName))
    val state = _state.asStateFlow()
    private val _events = Channel<SubCategoryDrilldownEvent>()
    val events = _events.receiveAsFlow()

    init { loadData(DateFilter.ThisMonth) }

    fun onAction(action: SubCategoryDrilldownAction) {
        when (action) {
            is SubCategoryDrilldownAction.OnFilterChange -> loadData(action.filter)
            is SubCategoryDrilldownAction.OnItemClick -> viewModelScope.launch {
                val item = _state.value.items.find { it.subCategoryId == action.subCategoryId }
                val title = item?.name ?: "Uncategorized"
                _events.send(SubCategoryDrilldownEvent.NavigateToFilteredExpenses(
                    categoryId = categoryId,
                    subCategoryId = action.subCategoryId ?: -1L,
                    title = "${_state.value.categoryName} › $title"
                ))
            }
            SubCategoryDrilldownAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(SubCategoryDrilldownEvent.NavigateToAddExpense)
            }
            SubCategoryDrilldownAction.OnBack -> viewModelScope.launch {
                _events.send(SubCategoryDrilldownEvent.NavigateBack)
            }
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            combine(
                categoryRepository.getSubCategoriesForCategory(categoryId),
                expenseRepository.getExpensesForCategory(categoryId, filter)
            ) { subCats, expenses ->
                val totalSpend = expenses.sumOf { it.amount }
                val grouped = expenses.groupBy { it.subCategory?.id }
                val items = mutableListOf<SubCategoryItem>()

                subCats.forEach { sub ->
                    val subTotal = grouped[sub.id]?.sumOf { it.amount } ?: 0.0
                    items.add(SubCategoryItem(
                        subCategoryId = sub.id,
                        name = sub.name,
                        total = subTotal,
                        percentage = if (totalSpend > 0) (subTotal / totalSpend) * 100 else 0.0
                    ))
                }

                val uncatTotal = grouped[null]?.sumOf { it.amount } ?: 0.0
                items.add(SubCategoryItem(
                    subCategoryId = null,
                    name = "Uncategorized",
                    total = uncatTotal,
                    percentage = if (totalSpend > 0) (uncatTotal / totalSpend) * 100 else 0.0
                ))

                _state.value.copy(
                    totalSpend = totalSpend,
                    items = items.sortedByDescending { it.total },
                    filter = filter,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }
}

@Composable
fun SubCategoryDrilldownRoot(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToFilteredExpenses: (Long, Long, String) -> Unit,
    viewModel: SubCategoryDrilldownViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SubCategoryDrilldownEvent.NavigateToFilteredExpenses ->
                onNavigateToFilteredExpenses(event.categoryId, event.subCategoryId, event.title)
            SubCategoryDrilldownEvent.NavigateToAddExpense -> onNavigateToAddExpense()
            SubCategoryDrilldownEvent.NavigateBack -> onNavigateBack()
        }
    }
    val state by viewModel.state.collectAsState()
    SubCategoryDrilldownScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryDrilldownScreen(state: SubCategoryDrilldownState, onAction: (SubCategoryDrilldownAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.categoryName, style = MaterialTheme.typography.titleMedium)
                        Text(state.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SubCategoryDrilldownAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(SubCategoryDrilldownAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add_expense)) },
                text = { Text(stringResource(Res.string.action_add)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(SubCategoryDrilldownAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.items.all { it.total == 0.0 }) {
                EmptyState(
                    title = stringResource(Res.string.drilldown_empty_title),
                    message = stringResource(Res.string.drilldown_empty_message),
                    actionLabel = stringResource(Res.string.action_add_expense),
                    onAction = { onAction(SubCategoryDrilldownAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items.filter { it.total > 0 || it.subCategoryId == null }, key = { it.subCategoryId ?: -1L }) { item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().animateItem()
                                .bounceClick { onAction(SubCategoryDrilldownAction.OnItemClick(item.subCategoryId)) },
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 1.dp
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (item.subCategoryId == null) stringResource(Res.string.common_uncategorized) else item.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(stringResource(Res.string.drilldown_percent_of_total, item.percentage.toPercentString()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(item.total.toCurrencyString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- Filtered Expenses ----

data class FilteredExpensesState(
    val title: String = "",
    val filter: DateFilter = DateFilter.ThisMonth,
    val grouped: Map<String, List<ExpenseWithDetails>> = emptyMap(),
    val isLoading: Boolean = true
)

sealed interface FilteredExpensesAction {
    data class OnFilterChange(val filter: DateFilter) : FilteredExpensesAction
    data class OnExpenseClick(val id: Long) : FilteredExpensesAction
    data object OnBack : FilteredExpensesAction
}

sealed interface FilteredExpensesEvent {
    data class NavigateToEdit(val id: Long) : FilteredExpensesEvent
    data object NavigateBack : FilteredExpensesEvent
}

class FilteredExpensesViewModel(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val categoryId: Long = savedStateHandle["categoryId"] ?: 0L
    private val subCategoryId: Long = savedStateHandle["subCategoryId"] ?: -1L
    private val title: String = savedStateHandle["title"] ?: ""

    private val _state = MutableStateFlow(FilteredExpensesState(title = title))
    val state = _state.asStateFlow()
    private val _events = Channel<FilteredExpensesEvent>()
    val events = _events.receiveAsFlow()

    init { loadData(DateFilter.ThisMonth) }

    fun onAction(action: FilteredExpensesAction) {
        when (action) {
            is FilteredExpensesAction.OnFilterChange -> loadData(action.filter)
            is FilteredExpensesAction.OnExpenseClick -> viewModelScope.launch {
                _events.send(FilteredExpensesEvent.NavigateToEdit(action.id))
            }
            FilteredExpensesAction.OnBack -> viewModelScope.launch {
                _events.send(FilteredExpensesEvent.NavigateBack)
            }
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            val actualSubCatId = if (subCategoryId == -1L) null else subCategoryId
            expenseRepository.getExpensesForCategoryAndSubCategory(categoryId, actualSubCatId, filter).collect { expenses ->
                _state.update { state ->
                    state.copy(
                        grouped = expenses.groupBy { it.expenseDate.toDisplayDate() },
                        isLoading = false
                    )
                }
            }
        }
    }
}

@Composable
fun FilteredExpensesRoot(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: FilteredExpensesViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FilteredExpensesEvent.NavigateToEdit -> onNavigateToEdit(event.id)
            FilteredExpensesEvent.NavigateBack -> onNavigateBack()
        }
    }
    val state by viewModel.state.collectAsState()
    FilteredExpensesScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredExpensesScreen(state: FilteredExpensesState, onAction: (FilteredExpensesAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = { onAction(FilteredExpensesAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(FilteredExpensesAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.grouped.isEmpty()) {
                EmptyState(title = stringResource(Res.string.filtered_empty_title), message = stringResource(Res.string.filtered_empty_message))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.grouped.forEach { (date, expenses) ->
                        item(key = "header_$date") {
                            Text(date, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(expenses, key = { it.id }) { expense ->
                            ExpenseItemCard(
                                name = expense.name,
                                amount = expense.amount.toCurrencyString(),
                                categoryName = expense.category.name,
                                categoryColor = hexToColor(expense.category.colorHex),
                                categoryIcon = expense.category.icon,
                                subCategoryName = expense.subCategory?.name,
                                time = expense.expenseDate.toDisplayTime(),
                                onClick = { onAction(FilteredExpensesAction.OnExpenseClick(expense.id)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
