package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.bounceClick
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.common_uncategorized
import expensetrackr.core.presentation.generated.resources.drilldown_percent_of_total
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubCategoryItemRow(
    item: SubCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().bounceClick { onClick() },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (item.subCategoryId == null) stringResource(Res.string.common_uncategorized) else item.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(stringResource(Res.string.drilldown_percent_of_total, item.percentage.toPercentString()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.total.toCurrencyString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview
@Composable
private fun SubCategoryItemRowPreview() {
    ExpenseTrackerTheme {
        SubCategoryItemRow(
            item = SubCategoryItem(subCategoryId = 1, name = "Dining out", total = 3200.0, percentage = 45.0),
            onClick = {}
        )
    }
}
