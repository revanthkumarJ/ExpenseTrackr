package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.revanthdev.expensetrackr.core.designsystem.component.GradientIconTile
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_delete
import expensetrackr.core.presentation.generated.resources.action_edit
import expensetrackr.core.presentation.generated.resources.category_default_badge
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CategoryRow(
    category: Category,
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
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GradientIconTile(category.icon, hexToColor(category.colorHex), size = 44)
            Spacer(Modifier.width(12.dp))
            Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            if (!category.isDefault) {
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, stringResource(Res.string.action_edit)) }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete), tint = MaterialTheme.colorScheme.error) }
            } else {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(stringResource(Res.string.category_default_badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoryRowPreview() {
    ExpenseTrackerTheme {
        CategoryRow(
            category = Category(1, "Food", "🍔", "#FF7043", createdAt = LocalDateTime(2024, 1, 1, 0, 0)),
            onEdit = {},
            onDelete = {}
        )
    }
}
