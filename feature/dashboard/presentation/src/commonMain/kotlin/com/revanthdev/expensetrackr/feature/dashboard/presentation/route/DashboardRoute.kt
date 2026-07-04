package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object DashboardRoute

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
