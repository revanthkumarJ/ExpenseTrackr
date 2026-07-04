package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.GradientIconTile
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import kotlinx.datetime.LocalDateTime
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun CategoryBudgetRow(
    category: Category,
    budgetText: String,
    onBudgetChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        GradientIconTile(category.icon, hexToColor(category.colorHex), size = 40)
        Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = budgetText,
            onValueChange = onBudgetChange,
            placeholder = { Text("0") },
            prefix = { Text("₹") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            visualTransformation = DecimalInputVisualTransformation(DecimalFormatter()),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(130.dp)
        )
    }
}

@Preview
@Composable
private fun CategoryBudgetRowPreview() {
    ExpenseTrackerTheme {
        CategoryBudgetRow(
            category = Category(
                id = 1,
                name = "Food",
                icon = "🍔",
                colorHex = "#FF7043",
                createdAt = LocalDateTime(2024, 1, 1, 0, 0)
            ),
            budgetText = "5000",
            onBudgetChange = {}
        )
    }
}
