package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import kotlinx.datetime.LocalDateTime

@Composable
internal fun LegendItem(stat: CategoryStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(4.dp), color = stat.color, modifier = Modifier.size(14.dp)) {}
            Text("${stat.category.icon} ${stat.category.name}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(stat.total.toCurrencyString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Surface(shape = RoundedCornerShape(6.dp), color = stat.color.copy(alpha = 0.14f)) {
                Text(
                    stat.percentage.toPercentString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = stat.color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun LegendItemPreview() {
    ExpenseTrackerTheme {
        LegendItem(
            stat = CategoryStat(
                category = Category(1, "Food", "🍔", "#FF7043", createdAt = LocalDateTime(2024, 1, 1, 0, 0)),
                total = 6000.0,
                percentage = 60.0,
                color = Color(0xFFFF7043)
            )
        )
    }
}
