package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class AddEditExpenseRoute(val expenseId: Long = -1L)

@Composable
fun AddEditExpenseRoot(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToAddSubCategory: () -> Unit,
    viewModel: AddEditExpenseViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddEditExpenseEvent.NavigateBack -> onNavigateBack()
            AddEditExpenseEvent.NavigateToAddCategory -> onNavigateToAddCategory()
            AddEditExpenseEvent.NavigateToAddSubCategory -> onNavigateToAddSubCategory()
        }
    }
    val state by viewModel.state.collectAsState()
    AddEditExpenseScreen(state = state, onAction = viewModel::onAction)
}
