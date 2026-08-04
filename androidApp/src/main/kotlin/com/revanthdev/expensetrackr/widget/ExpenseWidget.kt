package com.revanthdev.expensetrackr.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.revanthdev.expensetrackr.MainActivity
import com.revanthdev.expensetrackr.R
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext
import kotlin.time.Clock

/**
 * Home-screen widget: income vs expense for this month and for today, plus a quick-add button.
 *
 * Glance renders to RemoteViews, so nothing from `core:design-system` or the Compose Multiplatform
 * string catalog is usable here. Both the palette (res/values{,-night}/colors.xml) and the strings
 * are plain Android resources hand-mirrored from the app — keep them in step by hand.
 *
 * The widget follows the *system* locale, not the app's in-app language override: it lives in the
 * launcher's host and never sees `ProvideAppLocale`.
 */
class ExpenseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Koin is started in ExpenseTrackerApp. A widget update can run without any Activity, but
        // never without the Application, so the graph is always ready by this point.
        val repository: ExpenseRepository = GlobalContext.get().get()

        provideContent {
            // Collecting the repository Flows keeps the widget live: while the host holds the
            // session open, adding a transaction in the app recomposes this straight away.
            val today = todayFilter()
            val monthIncome by repository.getTotalIncome(DateFilter.ThisMonth).collectAsState(0.0)
            val monthExpense by repository.getTotalSpend(DateFilter.ThisMonth).collectAsState(0.0)
            val todayIncome by repository.getTotalIncome(today).collectAsState(0.0)
            val todayExpense by repository.getTotalSpend(today).collectAsState(0.0)

            WidgetContent(context, monthIncome, monthExpense, todayIncome, todayExpense)
        }
    }
}

/** "Today" as a single-day range — [DateFilter] has no Today case and doesn't need one. */
private fun todayFilter(): DateFilter {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return DateFilter.CustomRange(start = today, end = today)
}

// ── Palette ──────────────────────────────────────────────────────────────────
// Light/dark values live in res/values{,-night}/colors.xml — Glance 1.1.x offers no day/night
// ColorProvider factory, so resource qualifiers are how a widget switches theme. See that file
// for why these are fixed colours rather than GlanceTheme's dynamic ones.
private val Surface = ColorProvider(R.color.widget_surface)
private val OnSurface = ColorProvider(R.color.widget_on_surface)
private val OnSurfaceMuted = ColorProvider(R.color.widget_on_surface_muted)
private val Primary = ColorProvider(R.color.widget_primary)
private val OnPrimary = ColorProvider(R.color.widget_on_primary)
private val IncomeTint = ColorProvider(R.color.widget_income_tint)
private val IncomeText = ColorProvider(R.color.widget_income_text)
private val ExpenseTint = ColorProvider(R.color.widget_expense_tint)
private val ExpenseText = ColorProvider(R.color.widget_expense_text)

@Composable
private fun WidgetContent(
    context: Context,
    monthIncome: Double,
    monthExpense: Double,
    todayIncome: Double,
    todayExpense: Double,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Surface)
            .cornerRadius(24.dp)
            .padding(12.dp),
    ) {
        Header(context)
        Spacer(GlanceModifier.height(8.dp))
        // The two sections share whatever height is left after the header and button. Cell heights
        // are quantised, so the host almost never hands us exactly the content height; giving the
        // slack to the tiles keeps it invisible, whereas a flexible spacer would pool it into one
        // visible gap. Keep the widget near its content height (see expense_widget_info.xml) so
        // there is little to share out in the first place.
        PeriodSection(
            context = context,
            label = context.getString(R.string.widget_this_month),
            income = monthIncome,
            expense = monthExpense,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.height(8.dp))
        PeriodSection(
            context = context,
            label = context.getString(R.string.widget_today),
            income = todayIncome,
            expense = todayExpense,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.height(8.dp))
        AddTransactionButton(context)
    }
}

/** A labelled period — "THIS MONTH" / "TODAY" — over its income and expense tiles. */
@Composable
private fun PeriodSection(
    context: Context,
    label: String,
    income: Double,
    expense: Double,
    modifier: GlanceModifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(label)
        Spacer(GlanceModifier.height(5.dp))
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            StatCard(
                label = context.getString(R.string.widget_income),
                amount = income,
                tint = IncomeTint,
                textColor = IncomeText,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            StatCard(
                label = context.getString(R.string.widget_expense),
                amount = expense,
                tint = ExpenseTint,
                textColor = ExpenseText,
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun Header(context: Context) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = GlanceModifier.size(22.dp),
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = TextStyle(color = OnSurfaceMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium),
    )
}

@Composable
private fun StatCard(
    label: String,
    amount: Double,
    tint: ColorProvider,
    textColor: ColorProvider,
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier
            .background(tint)
            .cornerRadius(14.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TextStyle(color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = amount.toCurrencyString(),
            // maxLines guards the long-amount case: a six-figure total in a half-width card would
            // otherwise wrap and push the button out of the widget's fixed height.
            maxLines = 1,
            style = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun AddTransactionButton(context: Context) {
    // Deliberately a styled Row rather than Glance's Button: Button ignores cornerRadius on older
    // hosts, and this keeps the tap target the full width of the widget.
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(Primary)
            .cornerRadius(14.dp)
            .padding(vertical = 10.dp)
            .clickable(actionStartActivity(quickAddIntent(context))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = context.getString(R.string.widget_add_transaction),
            style = TextStyle(color = OnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        )
    }
}

/**
 * Opens the app straight on the Add Transaction form. `SINGLE_TOP` pairs with MainActivity's
 * launchMode so a running app receives this through `onNewIntent` instead of being recreated.
 */
private fun quickAddIntent(context: Context) =
    Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_QUICK_ADD
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
