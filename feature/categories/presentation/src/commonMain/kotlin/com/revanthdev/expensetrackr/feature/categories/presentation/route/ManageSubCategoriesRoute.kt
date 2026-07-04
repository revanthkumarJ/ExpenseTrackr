package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object ManageSubCategoriesRoute

@Composable
fun ManageSubCategoriesRoot(onNavigateBack: () -> Unit, viewModel: ManageSubCategoriesViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { ManageSubCategoriesEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    ManageSubCategoriesScreen(state = state, onAction = viewModel::onAction)
}
