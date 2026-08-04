package com.revanthdev.expensetrackr.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll

/** Manifest entry point for [ExpenseWidget]; Glance handles the AppWidgetProvider plumbing. */
class ExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpenseWidget()
}

/**
 * Redraws every placed widget.
 *
 * While a widget is on screen Glance keeps its session alive and the repository Flows in
 * [ExpenseWidget] push updates on their own. This covers the gap that leaves: edits made while
 * the widget's host wasn't listening, which would otherwise show stale totals until the next
 * system refresh. Called from `MainActivity.onStop`, i.e. when the user leaves for the launcher.
 */
suspend fun refreshExpenseWidgets(context: Context) {
    ExpenseWidget().updateAll(context)
}
