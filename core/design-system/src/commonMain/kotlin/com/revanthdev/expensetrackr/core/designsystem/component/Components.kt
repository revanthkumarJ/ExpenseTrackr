package com.revanthdev.expensetrackr.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_apply
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.card_budget
import expensetrackr.core.presentation.generated.resources.card_percent_of_spend
import expensetrackr.core.presentation.generated.resources.filter_custom
import expensetrackr.core.presentation.generated.resources.filter_last_month
import expensetrackr.core.presentation.generated.resources.filter_this_month
import expensetrackr.core.presentation.generated.resources.filter_this_week
import expensetrackr.core.presentation.generated.resources.filter_this_year
import org.jetbrains.compose.resources.stringResource
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetYellow
import com.revanthdev.expensetrackr.core.domain.model.DateFilter

/** A rounded tile holding a category emoji over a soft gradient of the category color. */
@Composable
fun GradientIconTile(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 48,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.12f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ExpenseItemCard(
    name: String,
    amount: String,
    categoryName: String,
    categoryColor: Color,
    categoryIcon: String,
    subCategoryName: String?,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().bounceClick(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GradientIconTile(categoryIcon, categoryColor)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryChip(categoryName, categoryColor)
                    if (subCategoryName != null) CategoryChip(subCategoryName, categoryColor)
                }
                Spacer(Modifier.height(3.dp))
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.14f), modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun CategoryCard(
    name: String,
    icon: String,
    color: Color,
    totalAmount: String,
    percentage: String,
    budgetAmount: String?,
    budgetProgress: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().bounceClick(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientIconTile(icon, color)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.14f)) {
                        Text(
                            stringResource(Res.string.card_percent_of_spend, percentage),
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    totalAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (budgetProgress != null && budgetAmount != null) {
                Spacer(Modifier.height(12.dp))
                val progressColor = when {
                    budgetProgress < 0.7f -> BudgetGreen
                    budgetProgress < 0.9f -> BudgetYellow
                    else -> BudgetRed
                }
                AnimatedProgressBar(
                    progress = budgetProgress.coerceIn(0f, 1f),
                    color = progressColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(Res.string.card_budget, budgetAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** A rounded progress bar whose fill animates smoothly to the target value. */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Int = 8,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "progress"
    )
    LinearProgressIndicator(
        progress = { animated },
        modifier = modifier.height(height.dp).clip(RoundedCornerShape(height.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.18f),
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    emoji: String = "💰",
    /** When set, the app logo image is shown instead of [emoji] in the hero tile. */
    logo: DrawableResource? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val visible = remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible.value = true }
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (logo != null) {
                    Image(
                        painter = painterResource(logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.extraLarge)
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onAction, shape = MaterialTheme.shapes.large) { Text(actionLabel) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterRow(
    selected: DateFilter,
    onSelect: (DateFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRangePicker by remember { mutableStateOf(false) }
    val isCustom = selected is DateFilter.CustomRange

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chipColors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
        listOf(
            DateFilter.ThisMonth to stringResource(Res.string.filter_this_month),
            DateFilter.ThisWeek to stringResource(Res.string.filter_this_week),
            DateFilter.LastMonth to stringResource(Res.string.filter_last_month),
            DateFilter.ThisYear to stringResource(Res.string.filter_this_year)
        ).forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(label) },
                shape = MaterialTheme.shapes.large,
                colors = chipColors,
                border = null
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { showRangePicker = true },
            label = { Text(if (isCustom) (selected as DateFilter.CustomRange).label() else stringResource(Res.string.filter_custom)) },
            leadingIcon = { Text("📅", style = MaterialTheme.typography.labelLarge) },
            shape = MaterialTheme.shapes.large,
            colors = chipColors,
            border = null
        )
    }

    if (showRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                onSelect(DateFilter.CustomRange(start, end))
                showRangePicker = false
            }
        )
    }
}

private fun DateFilter.CustomRange.label(): String =
    "${start.monthNumber}/${start.dayOfMonth} – ${end.monthNumber}/${end.dayOfMonth}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMs = state.selectedStartDateMillis
                    val endMs = state.selectedEndDateMillis
                    if (startMs != null && endMs != null) {
                        onConfirm(startMs.toUtcLocalDate(), endMs.toUtcLocalDate())
                    }
                },
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null
            ) { Text(stringResource(Res.string.action_apply)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } }
    ) {
        DateRangePicker(state = state, modifier = Modifier.weight(1f))
    }
}

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
