package com.revanthdev.expensetrackr.feature.settings.presentation.sync

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.sync_exported
import expensetrackr.core.presentation.generated.resources.sync_failed
import expensetrackr.core.presentation.generated.resources.sync_no_backup
import expensetrackr.core.presentation.generated.resources.sync_restored
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object SyncRoute

@Composable
fun SyncRoot(onBack: () -> Unit, viewModel: SyncViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingResult by remember { mutableStateOf<SyncResult?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SyncEvent.Show -> pendingResult = event.result
        }
    }

    pendingResult?.let { result ->
        val message = result.toMessage()
        LaunchedEffect(result) {
            snackbarHostState.showSnackbar(message)
            pendingResult = null
        }
    }

    SyncScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
private fun SyncResult.toMessage(): String = when (this) {
    is SyncResult.Exported -> stringResource(Res.string.sync_exported, expenses, incomes, location)
    is SyncResult.Restored -> stringResource(Res.string.sync_restored, count)
    SyncResult.NoBackupFound -> stringResource(Res.string.sync_no_backup)
    SyncResult.Failed -> stringResource(Res.string.sync_failed)
}
