package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.revanthdev.expensetrackr.core.domain.repository.FileOpenResult
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.downloads_failed
import expensetrackr.core.presentation.generated.resources.downloads_no_data
import expensetrackr.core.presentation.generated.resources.downloads_saved
import expensetrackr.core.presentation.generated.resources.downloads_saved_no_app
import expensetrackr.core.presentation.generated.resources.downloads_saved_open_failed
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object DownloadsRoute

@Composable
fun DownloadsRoot(onBack: () -> Unit, viewModel: DownloadsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingResult by remember { mutableStateOf<DownloadResult?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DownloadsEvent.Show -> pendingResult = event.result
        }
    }

    pendingResult?.let { result ->
        val message = result.toMessage()
        LaunchedEffect(result) {
            snackbarHostState.showSnackbar(message)
            pendingResult = null
        }
    }

    DownloadsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
private fun DownloadResult.toMessage(): String = when (this) {
    // The file is saved in every Saved branch; only the "and then?" part differs.
    is DownloadResult.Saved -> when (opened) {
        FileOpenResult.Opened ->
            stringResource(Res.string.downloads_saved, fileName, transactions, location)
        FileOpenResult.NoAppFound ->
            stringResource(Res.string.downloads_saved_no_app, fileName, location)
        FileOpenResult.Failed ->
            stringResource(Res.string.downloads_saved_open_failed, fileName, location)
    }

    DownloadResult.NoData -> stringResource(Res.string.downloads_no_data)
    DownloadResult.Failed -> stringResource(Res.string.downloads_failed)
}
