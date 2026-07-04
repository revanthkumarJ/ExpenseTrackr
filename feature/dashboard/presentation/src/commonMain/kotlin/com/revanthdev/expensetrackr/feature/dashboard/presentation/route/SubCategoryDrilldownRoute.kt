package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class SubCategoryRoute(val categoryId: Long, val categoryName: String)

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
