package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.component.ExpenseItemCard
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import com.revanthdev.expensetrackr.core.presentation.UiText
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayTime
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object AllExpensesRoute

@Serializable
data class EditExpenseRoute(val expenseId: Long)

data class ExpensesState(
    val grouped: Map<String, List<ExpenseWithDetails>> = emptyMap(),
    val filter: DateFilter = DateFilter.ThisMonth,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val snackbarMessage: UiText? = null
)

sealed interface ExpensesAction {
    data class OnFilterChange(val filter: DateFilter) : ExpensesAction
    data class OnSearchChange(val query: String) : ExpensesAction
    data class OnExpenseClick(val id: Long) : ExpensesAction
    data class OnDeleteExpense(val id: Long) : ExpensesAction
    data object OnAddExpenseClick : ExpensesAction
    data object OnSnackbarDismiss : ExpensesAction
}

sealed interface ExpensesEvent {
    data class NavigateToEdit(val id: Long) : ExpensesEvent
    data object NavigateToAddExpense : ExpensesEvent
    data class ShowSnackbar(val message: UiText) : ExpensesEvent
}

class ExpensesViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {
    private val _state = MutableStateFlow(ExpensesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ExpensesEvent>()
    val events = _events.receiveAsFlow()

    private var allExpenses: List<ExpenseWithDetails> = emptyList()

    init { loadExpenses(DateFilter.ThisMonth) }

    fun onAction(action: ExpensesAction) {
        when (action) {
            is ExpensesAction.OnFilterChange -> loadExpenses(action.filter)
            is ExpensesAction.OnSearchChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                applySearch(action.query)
            }
            is ExpensesAction.OnExpenseClick -> viewModelScope.launch {
                _events.send(ExpensesEvent.NavigateToEdit(action.id))
            }
            is ExpensesAction.OnDeleteExpense -> deleteExpense(action.id)
            ExpensesAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(ExpensesEvent.NavigateToAddExpense)
            }
            ExpensesAction.OnSnackbarDismiss -> _state.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun loadExpenses(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            expenseRepository.getExpensesWithDetails(filter).collect { expenses ->
                allExpenses = expenses
                val query = _state.value.searchQuery
                val filtered = if (query.isBlank()) expenses else filterExpenses(expenses, query)
                _state.update { it.copy(grouped = groupByDate(filtered), isLoading = false) }
            }
        }
    }

    private fun applySearch(query: String) {
        val filtered = if (query.isBlank()) allExpenses else filterExpenses(allExpenses, query)
        _state.update { it.copy(grouped = groupByDate(filtered)) }
    }

    private fun filterExpenses(expenses: List<ExpenseWithDetails>, query: String): List<ExpenseWithDetails> {
        val q = query.lowercase()
        return expenses.filter {
            it.name.lowercase().contains(q) ||
            it.category.name.lowercase().contains(q) ||
            it.amount.toString().contains(q)
        }
    }

    private fun groupByDate(expenses: List<ExpenseWithDetails>): Map<String, List<ExpenseWithDetails>> =
        expenses.groupBy { it.expenseDate.toDisplayDate() }

    private fun deleteExpense(id: Long) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(id)
            _events.send(ExpensesEvent.ShowSnackbar(UiText.DynamicString("Expense deleted")))
        }
    }
}

@Composable
fun AllExpensesRoot(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: ExpensesViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExpensesEvent.NavigateToEdit -> onNavigateToEdit(event.id)
            ExpensesEvent.NavigateToAddExpense -> onNavigateToAddExpense()
            is ExpensesEvent.ShowSnackbar -> {
                val msg = (event.message as? UiText.DynamicString)?.value ?: ""
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }
    val state by viewModel.state.collectAsState()
    AllExpensesScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllExpensesScreen(
    state: ExpensesState,
    onAction: (ExpensesAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("All Expenses") })
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = state.searchQuery,
                            onQueryChange = { onAction(ExpensesAction.OnSearchChange(it)) },
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text("Search expenses...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, null) }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {}
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ExpensesAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, "Add Expense") },
                text = { Text("Add") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(ExpensesAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.grouped.isEmpty()) {
                EmptyState(
                    title = "No expenses",
                    message = "Tap + to add your first expense",
                    actionLabel = "Add Expense",
                    onAction = { onAction(ExpensesAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.grouped.forEach { (date, expenses) ->
                        item(key = "header_$date") {
                            Text(
                                date,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
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
                                onClick = { onAction(ExpensesAction.OnExpenseClick(expense.id)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

val expensesModule = org.koin.dsl.module {
    viewModelOf(::ExpensesViewModel)
}
