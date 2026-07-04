package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.CategoryCard
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_add
import expensetrackr.core.presentation.generated.resources.action_add_expense
import expensetrackr.core.presentation.generated.resources.app_logo
import expensetrackr.core.presentation.generated.resources.dashboard_empty_message
import expensetrackr.core.presentation.generated.resources.dashboard_empty_title
import expensetrackr.core.presentation.generated.resources.filter_last_month
import expensetrackr.core.presentation.generated.resources.filter_this_week
import expensetrackr.core.presentation.generated.resources.period_custom_range
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val periodLabel = when (state.filter) {
                        DateFilter.ThisWeek -> stringResource(Res.string.filter_this_week)
                        DateFilter.LastMonth -> stringResource(Res.string.filter_last_month)
                        is DateFilter.CustomRange -> stringResource(Res.string.period_custom_range)
                        else -> state.monthLabel
                    }
                    Column {
                        Text(periodLabel, style = MaterialTheme.typography.titleMedium)
                        Text(state.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(DashboardAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add_expense)) },
                text = { Text(stringResource(Res.string.action_add)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(DashboardAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.overallBudget != null && state.overallProgress != null) {
                BudgetProgressBar(
                    spent = state.totalSpend.toCurrencyString(),
                    budget = state.overallBudget.toCurrencyString(),
                    progress = state.overallProgress,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.monthlySalary != null) {
                SalaryRemainingCard(
                    salary = state.monthlySalary,
                    spent = state.totalSpend,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.categories.isEmpty()) {
                EmptyState(
                    title = stringResource(Res.string.dashboard_empty_title),
                    message = stringResource(Res.string.dashboard_empty_message),
                    logo = Res.drawable.app_logo,
                    actionLabel = stringResource(Res.string.action_add_expense),
                    onAction = { onAction(DashboardAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories, key = { it.category.id }) { catUi ->
                        CategoryCard(
                            name = catUi.category.name,
                            icon = catUi.category.icon,
                            color = hexToColor(catUi.category.colorHex),
                            totalAmount = catUi.total.toCurrencyString(),
                            percentage = catUi.percentage.toPercentString(),
                            budgetAmount = catUi.category.budgetAmount?.toCurrencyString(),
                            budgetProgress = catUi.budgetProgress,
                            onClick = { onAction(DashboardAction.OnCategoryClick(catUi.category.id, catUi.category.name)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
