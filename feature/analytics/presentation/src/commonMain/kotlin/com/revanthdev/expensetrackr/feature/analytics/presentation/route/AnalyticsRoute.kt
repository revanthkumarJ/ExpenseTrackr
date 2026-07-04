package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object AnalyticsRoute

@Composable
fun AnalyticsRoot(viewModel: AnalyticsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    AnalyticsScreen(state = state, onAction = viewModel::onAction)
}
