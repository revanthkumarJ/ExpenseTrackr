package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object BudgetRoute

@Composable
fun BudgetRoot(onNavigateBack: () -> Unit, viewModel: BudgetViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { BudgetEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    BudgetScreen(state = state, onAction = viewModel::onAction)
}
