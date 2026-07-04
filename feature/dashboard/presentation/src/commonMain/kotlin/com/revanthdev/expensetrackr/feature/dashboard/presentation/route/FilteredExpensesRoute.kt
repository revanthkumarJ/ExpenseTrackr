package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class FilteredExpensesRoute(val categoryId: Long, val subCategoryId: Long = -1L, val title: String = "")

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
