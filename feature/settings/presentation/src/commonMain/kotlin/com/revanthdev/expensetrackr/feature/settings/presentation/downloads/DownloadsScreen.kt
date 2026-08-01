package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.DateRangePickerDialog
import com.revanthdev.expensetrackr.core.designsystem.component.bounceClick
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.downloads_description
import expensetrackr.core.presentation.generated.resources.downloads_excel_subtitle
import expensetrackr.core.presentation.generated.resources.downloads_excel_title
import expensetrackr.core.presentation.generated.resources.downloads_generating
import expensetrackr.core.presentation.generated.resources.downloads_pdf_subtitle
import expensetrackr.core.presentation.generated.resources.downloads_pdf_title
import expensetrackr.core.presentation.generated.resources.downloads_period_last_1_year
import expensetrackr.core.presentation.generated.resources.downloads_period_last_3_months
import expensetrackr.core.presentation.generated.resources.downloads_period_title
import expensetrackr.core.presentation.generated.resources.downloads_title
import expensetrackr.core.presentation.generated.resources.filter_last_month
import expensetrackr.core.presentation.generated.resources.filter_this_month
import expensetrackr.core.presentation.generated.resources.period_custom_range
import expensetrackr.core.presentation.generated.resources.sync_location
import org.jetbrains.compose.resources.stringResource

private val PDF_ACCENT = Color(0xFFD64545)
private val EXCEL_ACCENT = Color(0xFF1B8A3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadsScreen(
    state: DownloadsState,
    snackbarHostState: SnackbarHostState,
    onAction: (DownloadsAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.downloads_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(Res.string.downloads_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(Res.string.downloads_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(Res.string.sync_location, state.locationLabel),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }

            FormatCard(
                icon = Icons.Rounded.PictureAsPdf,
                accent = PDF_ACCENT,
                title = stringResource(Res.string.downloads_pdf_title),
                subtitle = stringResource(Res.string.downloads_pdf_subtitle),
                isGenerating = state.generating == ReportFormat.PDF,
                enabled = !state.isBusy,
                onClick = { onAction(DownloadsAction.OnFormatClick(ReportFormat.PDF)) },
            )
            FormatCard(
                icon = Icons.Rounded.TableChart,
                accent = EXCEL_ACCENT,
                title = stringResource(Res.string.downloads_excel_title),
                subtitle = stringResource(Res.string.downloads_excel_subtitle),
                isGenerating = state.generating == ReportFormat.EXCEL,
                enabled = !state.isBusy,
                onClick = { onAction(DownloadsAction.OnFormatClick(ReportFormat.EXCEL)) },
            )

            if (state.isBusy) {
                Text(
                    stringResource(Res.string.downloads_generating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    PeriodPicker(
        isOpen = state.choosingPeriodFor != null,
        onDismiss = { onAction(DownloadsAction.OnDismissPeriodPicker) },
        onPicked = { onAction(DownloadsAction.OnPeriodPicked(it)) },
    )
}

@Composable
private fun FormatCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    isGenerating: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.bounceClick(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = accent.copy(alpha = 0.14f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Asks which stretch of time the report should cover. Choosing "Custom Range" swaps this dialog
 * for the Material date-range picker rather than stacking two dialogs.
 */
@Composable
private fun PeriodPicker(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onPicked: (ReportPeriod) -> Unit,
) {
    var showRangePicker by remember { mutableStateOf(false) }
    LaunchedEffect(isOpen) { if (!isOpen) showRangePicker = false }
    if (!isOpen) return

    if (showRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                showRangePicker = false
                onPicked(ReportPeriod.Custom(start, end))
            },
        )
        return
    }

    val options = listOf(
        ReportPeriod.ThisMonth to stringResource(Res.string.filter_this_month),
        ReportPeriod.LastMonth to stringResource(Res.string.filter_last_month),
        ReportPeriod.LastThreeMonths to stringResource(Res.string.downloads_period_last_3_months),
        ReportPeriod.LastYear to stringResource(Res.string.downloads_period_last_1_year),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.downloads_period_title)) },
        text = {
            Column {
                options.forEach { (period, label) ->
                    PeriodRow(label) { onPicked(period) }
                }
                PeriodRow(stringResource(Res.string.period_custom_range)) { showRangePicker = true }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Composable
private fun PeriodRow(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().bounceClick(onClick = onClick),
        color = Color.Transparent,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
