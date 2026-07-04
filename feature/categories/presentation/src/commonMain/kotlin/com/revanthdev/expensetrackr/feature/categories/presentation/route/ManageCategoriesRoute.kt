package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object ManageCategoriesRoute

@Composable
fun ManageCategoriesRoot(onNavigateBack: () -> Unit, viewModel: ManageCategoriesViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { ManageCategoriesEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    ManageCategoriesScreen(state = state, onAction = viewModel::onAction)
}
