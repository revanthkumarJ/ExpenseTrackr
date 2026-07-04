package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.DateFilterRow
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.analytics_avg_day
import expensetrackr.core.presentation.generated.resources.analytics_breakdown
import expensetrackr.core.presentation.generated.resources.analytics_empty_message
import expensetrackr.core.presentation.generated.resources.analytics_empty_title
import expensetrackr.core.presentation.generated.resources.analytics_income
import expensetrackr.core.presentation.generated.resources.analytics_saved
import expensetrackr.core.presentation.generated.resources.analytics_savings_rate
import expensetrackr.core.presentation.generated.resources.analytics_spent_vs_saved
import expensetrackr.core.presentation.generated.resources.analytics_top_category
import expensetrackr.core.presentation.generated.resources.common_total
import expensetrackr.core.presentation.generated.resources.nav_analytics
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(state: AnalyticsState, onAction: (AnalyticsAction) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.nav_analytics)) }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Filter chips are always visible so the user can switch periods even when a
            // selected period happens to have no data.
            DateFilterRow(
                selected = state.filter,
                onSelect = { onAction(AnalyticsAction.OnFilterChange(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                state.isLoading -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.categoryStats.isEmpty() -> EmptyState(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.analytics_empty_title),
                    message = stringResource(Res.string.analytics_empty_message),
                    emoji = "📊"
                )

                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DonutChart(
                            stats = state.categoryStats,
                            centerLabel = stringResource(Res.string.common_total),
                            centerValue = state.totalSpend.toCurrencyString(),
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(stringResource(Res.string.analytics_top_category), state.highestCategory, Modifier.weight(1f))
                            StatCard(stringResource(Res.string.analytics_avg_day), state.avgDailySpend, Modifier.weight(1f))
                        }
                    }
                    if (state.income > 0) {
                        item {
                            Text(
                                stringResource(Res.string.analytics_spent_vs_saved),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(stringResource(Res.string.analytics_income), state.income.toCurrencyString(), Modifier.weight(1f))
                                StatCard(
                                    stringResource(Res.string.analytics_saved),
                                    state.saved.toCurrencyString(),
                                    Modifier.weight(1f)
                                )
                                StatCard(
                                    stringResource(Res.string.analytics_savings_rate),
                                    state.savingsRate.coerceAtLeast(0f).let { "${(it * 100).toInt()}%" },
                                    Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            SpentVsSavedChart(
                                spent = state.totalSpend,
                                saved = state.saved,
                                income = state.income,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (state.spendByTime.isNotEmpty()) {
                        item {
                            SpendingBarChart(
                                buckets = state.spendByTime,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Text(stringResource(Res.string.analytics_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    items(state.categoryStats, key = { it.category.id }) { stat ->
                        LegendItem(stat, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}
