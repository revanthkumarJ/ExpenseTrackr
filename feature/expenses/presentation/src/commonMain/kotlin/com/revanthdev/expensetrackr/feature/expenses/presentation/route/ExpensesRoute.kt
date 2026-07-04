package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.expense_deleted
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object AllExpensesRoute

@Serializable
data class EditExpenseRoute(val expenseId: Long)

@Composable
fun AllExpensesRoot(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAddExpense: () -> Unit,
    viewModel: ExpensesViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(Res.string.expense_deleted)
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExpensesEvent.NavigateToEdit -> onNavigateToEdit(event.id)
            ExpensesEvent.NavigateToAddExpense -> onNavigateToAddExpense()
            is ExpensesEvent.ShowSnackbar -> {
                scope.launch { snackbarHostState.showSnackbar(deletedMessage) }
            }
        }
    }
    val state by viewModel.state.collectAsState()
    AllExpensesScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}
