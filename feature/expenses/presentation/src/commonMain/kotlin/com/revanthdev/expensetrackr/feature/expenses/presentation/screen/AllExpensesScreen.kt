package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.component.ExpenseItemCard
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayTime
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_add
import expensetrackr.core.presentation.generated.resources.action_add_expense
import expensetrackr.core.presentation.generated.resources.app_logo
import expensetrackr.core.presentation.generated.resources.expenses_empty_message
import expensetrackr.core.presentation.generated.resources.expenses_empty_title
import expensetrackr.core.presentation.generated.resources.expenses_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllExpensesScreen(
    state: ExpensesState,
    onAction: (ExpensesAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text(stringResource(Res.string.expenses_title)) })
                ExpensesSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onAction(ExpensesAction.OnSearchChange(it)) }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ExpensesAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add_expense)) },
                text = { Text(stringResource(Res.string.action_add)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(ExpensesAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.grouped.isEmpty()) {
                EmptyState(
                    title = stringResource(Res.string.expenses_empty_title),
                    message = stringResource(Res.string.expenses_empty_message),
                    logo = Res.drawable.app_logo,
                    actionLabel = stringResource(Res.string.action_add_expense),
                    onAction = { onAction(ExpensesAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.grouped.forEach { (date, expenses) ->
                        item(key = "header_$date") {
                            Text(
                                date,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(expenses, key = { it.id }) { expense ->
                            ExpenseItemCard(
                                name = expense.name,
                                amount = expense.amount.toCurrencyString(),
                                categoryName = expense.category.name,
                                categoryColor = hexToColor(expense.category.colorHex),
                                categoryIcon = expense.category.icon,
                                subCategoryName = expense.subCategory?.name,
                                time = expense.expenseDate.toDisplayTime(),
                                onClick = { onAction(ExpensesAction.OnExpenseClick(expense.id)) },
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
}
