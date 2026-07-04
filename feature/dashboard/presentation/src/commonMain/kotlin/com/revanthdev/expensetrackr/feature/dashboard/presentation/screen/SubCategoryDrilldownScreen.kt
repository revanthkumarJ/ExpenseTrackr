package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_add
import expensetrackr.core.presentation.generated.resources.action_add_expense
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.drilldown_empty_message
import expensetrackr.core.presentation.generated.resources.drilldown_empty_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryDrilldownScreen(state: SubCategoryDrilldownState, onAction: (SubCategoryDrilldownAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.categoryName, style = MaterialTheme.typography.titleMedium)
                        Text(state.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SubCategoryDrilldownAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(SubCategoryDrilldownAction.OnAddExpenseClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add_expense)) },
                text = { Text(stringResource(Res.string.action_add)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(SubCategoryDrilldownAction.OnFilterChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.items.all { it.total == 0.0 }) {
                EmptyState(
                    title = stringResource(Res.string.drilldown_empty_title),
                    message = stringResource(Res.string.drilldown_empty_message),
                    actionLabel = stringResource(Res.string.action_add_expense),
                    onAction = { onAction(SubCategoryDrilldownAction.OnAddExpenseClick) }
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items.filter { it.total > 0 || it.subCategoryId == null }, key = { it.subCategoryId ?: -1L }) { item ->
                        SubCategoryItemRow(
                            item = item,
                            onClick = { onAction(SubCategoryDrilldownAction.OnItemClick(item.subCategoryId)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
