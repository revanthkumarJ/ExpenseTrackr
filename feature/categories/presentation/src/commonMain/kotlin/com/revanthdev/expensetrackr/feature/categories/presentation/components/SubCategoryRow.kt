package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_delete
import expensetrackr.core.presentation.generated.resources.action_edit
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SubCategoryRow(
    subCategory: SubCategory,
    parentCategory: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(subCategory.name, style = MaterialTheme.typography.titleSmall)
                if (parentCategory != null) {
                    Text("${parentCategory.icon} ${parentCategory.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, stringResource(Res.string.action_edit)) }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete), tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Preview
@Composable
private fun SubCategoryRowPreview() {
    val createdAt = LocalDateTime(2024, 1, 1, 0, 0)
    ExpenseTrackerTheme {
        SubCategoryRow(
            subCategory = SubCategory(1, "Dining out", 1, createdAt),
            parentCategory = Category(1, "Food", "🍔", "#FF7043", createdAt = createdAt),
            onEdit = {},
            onDelete = {}
        )
    }
}
