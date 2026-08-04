package com.revanthdev.expensetrackr

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.AppUpdateStatus
import com.revanthdev.expensetrackr.core.presentation.LocalAppUpdateManager
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_later
import expensetrackr.core.presentation.generated.resources.action_restart
import expensetrackr.core.presentation.generated.resources.nav_analytics
import expensetrackr.core.presentation.generated.resources.nav_dashboard
import expensetrackr.core.presentation.generated.resources.nav_expenses
import expensetrackr.core.presentation.generated.resources.nav_settings
import expensetrackr.core.presentation.generated.resources.update_ready_message
import expensetrackr.core.presentation.generated.resources.update_ready_title
import org.jetbrains.compose.resources.stringResource
import com.revanthdev.expensetrackr.feature.analytics.presentation.AnalyticsRoot
import com.revanthdev.expensetrackr.feature.analytics.presentation.AnalyticsRoute
import com.revanthdev.expensetrackr.feature.applock.presentation.AppLockRoot
import com.revanthdev.expensetrackr.feature.applock.presentation.AppLockRoute
import com.revanthdev.expensetrackr.feature.budget.presentation.BudgetRoot
import com.revanthdev.expensetrackr.feature.budget.presentation.BudgetRoute
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageCategoriesRoot
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageCategoriesRoute
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageSubCategoriesRoot
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageSubCategoriesRoute
import com.revanthdev.expensetrackr.feature.dashboard.presentation.DashboardRoot
import com.revanthdev.expensetrackr.feature.dashboard.presentation.DashboardRoute
import com.revanthdev.expensetrackr.feature.dashboard.presentation.FilteredExpensesRoot
import com.revanthdev.expensetrackr.feature.dashboard.presentation.FilteredExpensesRoute
import com.revanthdev.expensetrackr.feature.dashboard.presentation.SubCategoryDrilldownRoot
import com.revanthdev.expensetrackr.feature.dashboard.presentation.SubCategoryRoute
import com.revanthdev.expensetrackr.feature.expenses.presentation.AddEditExpenseRoot
import com.revanthdev.expensetrackr.feature.expenses.presentation.AddEditExpenseRoute
import com.revanthdev.expensetrackr.feature.expenses.presentation.AllExpensesRoot
import com.revanthdev.expensetrackr.feature.expenses.presentation.AllExpensesRoute
import com.revanthdev.expensetrackr.feature.onboarding.presentation.OnboardingRoot
import com.revanthdev.expensetrackr.feature.onboarding.presentation.OnboardingRoute
import com.revanthdev.expensetrackr.feature.settings.presentation.*
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.DownloadsRoot
import com.revanthdev.expensetrackr.feature.settings.presentation.downloads.DownloadsRoute
import com.revanthdev.expensetrackr.feature.settings.presentation.sync.SyncRoot
import com.revanthdev.expensetrackr.feature.settings.presentation.sync.SyncRoute
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
private data object MainRoute

// ---- Screen transition specs ----
private const val NAV_DURATION = 380

private val navEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(NAV_DURATION))
}
private val navExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(NAV_DURATION))
}
private val navPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(NAV_DURATION))
}
private val navPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(NAV_DURATION))
}

// Tabs are siblings — cross-fade with a gentle zoom instead of sliding.
private val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(280)) + scaleIn(initialScale = 0.96f, animationSpec = tween(280))
}
private val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(200))
}

/**
 * @param quickAddRequest incremented by the host each time the user asks to jump straight to the
 *   Add Transaction form (Android's home-screen widget). A counter, not a flag, so a second
 *   request while the app is already open is still observable. `0` means "no request".
 */
@Composable
fun App(quickAddRequest: Int = 0) {
    val settingsRepository = koinInject<SettingsRepository>()
    var startDestination by remember { mutableStateOf<Any?>(null) }
    val settingsState by settingsRepository.getSettings().collectAsState(initial = null)

    LaunchedEffect(Unit) {
        val settings = settingsRepository.getSettings().first()
        startDestination = when {
            !settings.isOnboardingDone -> OnboardingRoute
            settings.appLockType.name != "NONE" -> AppLockRoute
            else -> MainRoute
        }
    }

    ProvideAppLocale(settingsState?.language) {
        ExpenseTrackerTheme(darkTheme = settingsState?.isDarkMode ?: isSystemInDarkTheme()) {
            if (startDestination == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@ExpenseTrackerTheme
            }
            AppNavHost(startDestination = startDestination!!, quickAddRequest = quickAddRequest)
            AppUpdatePrompt()
        }
    }
}

/**
 * Drives the in-app update UX for the whole app. The platform bridge only reports status and
 * performs the steps — *when* to ask lives here so every platform behaves the same.
 *
 * On a platform with no store integration the status never leaves [AppUpdateStatus.Unknown], so
 * this composable does nothing at all.
 */
@Composable
private fun AppUpdatePrompt() {
    val updateManager = LocalAppUpdateManager.current
    val status by updateManager.status.collectAsState()
    // Ask at most once per process: without this, declining the flow re-triggers it immediately
    // (status falls back to Available), trapping the user in a loop.
    var promptedThisSession by rememberSaveable { mutableStateOf(false) }
    var installDismissed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(status) {
        if (status == AppUpdateStatus.Available && !promptedThisSession) {
            promptedThisSession = true
            updateManager.startUpdate()
        }
    }

    if (status == AppUpdateStatus.ReadyToInstall && !installDismissed) {
        AlertDialog(
            onDismissRequest = { installDismissed = true },
            title = { Text(stringResource(Res.string.update_ready_title)) },
            text = { Text(stringResource(Res.string.update_ready_message)) },
            confirmButton = {
                TextButton(onClick = { updateManager.completeUpdate() }) {
                    Text(stringResource(Res.string.action_restart))
                }
            },
            dismissButton = {
                TextButton(onClick = { installDismissed = true }) {
                    Text(stringResource(Res.string.action_later))
                }
            },
        )
    }
}

/** Replays a quick-add that was requested before the user had cleared onboarding / app lock. */
private fun consumeQuickAdd(
    navController: androidx.navigation.NavController,
    pending: Boolean,
    onConsumed: () -> Unit,
) {
    if (!pending) return
    onConsumed()
    navController.navigate(AddEditExpenseRoute())
}

@Composable
private fun AppNavHost(startDestination: Any, quickAddRequest: Int = 0) {
    val rootNavController = rememberNavController()

    // A quick-add asked for while the app is locked (or still onboarding) can't be honoured yet —
    // it's held here and replayed once the user reaches the main app, so the widget never becomes
    // a way past the PIN screen.
    var quickAddPending by remember { mutableStateOf(false) }

    LaunchedEffect(quickAddRequest) {
        if (quickAddRequest == 0) return@LaunchedEffect
        if (startDestination === MainRoute) {
            rootNavController.navigate(AddEditExpenseRoute())
        } else {
            quickAddPending = true
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = startDestination,
        enterTransition = navEnter,
        exitTransition = navExit,
        popEnterTransition = navPopEnter,
        popExitTransition = navPopExit
    ) {
        composable<OnboardingRoute> {
            OnboardingRoot(onNavigateToMain = {
                rootNavController.navigate(MainRoute) {
                    popUpTo(OnboardingRoute) { inclusive = true }
                }
                consumeQuickAdd(rootNavController, quickAddPending) { quickAddPending = false }
            })
        }

        composable<AppLockRoute> {
            AppLockRoot(
                onUnlocked = {
                    rootNavController.navigate(MainRoute) {
                        popUpTo(AppLockRoute) { inclusive = true }
                    }
                    consumeQuickAdd(rootNavController, quickAddPending) { quickAddPending = false }
                }
            )
        }

        composable<MainRoute> { MainScaffold(rootNavController = rootNavController) }

        composable<AddEditExpenseRoute> {
            AddEditExpenseRoot(
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToAddCategory = { rootNavController.navigate(ManageCategoriesRoute) },
                onNavigateToAddSubCategory = { rootNavController.navigate(ManageSubCategoriesRoute) }
            )
        }

        composable<BudgetRoute> {
            BudgetRoot(onNavigateBack = { rootNavController.popBackStack() })
        }

        composable<ManageCategoriesRoute> {
            ManageCategoriesRoot(onNavigateBack = { rootNavController.popBackStack() })
        }

        composable<ManageSubCategoriesRoute> {
            ManageSubCategoriesRoot(onNavigateBack = { rootNavController.popBackStack() })
        }

        composable<SubCategoryRoute> {
            SubCategoryDrilldownRoot(
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToAddExpense = { rootNavController.navigate(AddEditExpenseRoute()) },
                onNavigateToFilteredExpenses = { catId, subCatId, title ->
                    rootNavController.navigate(FilteredExpensesRoute(catId, subCatId, title))
                }
            )
        }

        composable<FilteredExpensesRoute> {
            FilteredExpensesRoot(
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToEdit = { id -> rootNavController.navigate(AddEditExpenseRoute(id)) }
            )
        }

        composable<NotificationSettingsRoute> {
            NotificationSettingsRoot(onBack = { rootNavController.popBackStack() })
        }

        composable<AppLockSetupRoute> {
            AppLockSetupRoot(onBack = { rootNavController.popBackStack() })
        }

        composable<AboutRoute> {
            AboutScreen(onBack = { rootNavController.popBackStack() })
        }

        composable<PrivacyPolicyRoute> {
            PrivacyPolicyScreen(onBack = { rootNavController.popBackStack() })
        }

        composable<TermsOfServiceRoute> {
            TermsOfServiceScreen(onBack = { rootNavController.popBackStack() })
        }

        composable<SyncRoute> {
            SyncRoot(onBack = { rootNavController.popBackStack() })
        }

        composable<DownloadsRoute> {
            DownloadsRoot(onBack = { rootNavController.popBackStack() })
        }

        composable<ThemeSettingsRoute> {
            ThemeSettingsRoot(onBack = { rootNavController.popBackStack() })
        }

        composable<LanguageSettingsRoute> {
            LanguageSettingsRoot(onBack = { rootNavController.popBackStack() })
        }
    }
}

@Composable
private fun MainScaffold(rootNavController: androidx.navigation.NavController) {
    val tabNavController = rememberNavController()
    val currentEntry by tabNavController.currentBackStackEntryAsState()

    val bottomNavItems = listOf(
        Triple(DashboardRoute as Any, Icons.Rounded.PieChart, stringResource(Res.string.nav_dashboard)),
        Triple(AllExpensesRoute as Any, Icons.Rounded.Receipt, stringResource(Res.string.nav_expenses)),
        Triple(AnalyticsRoute as Any, Icons.Rounded.BarChart, stringResource(Res.string.nav_analytics)),
        Triple(SettingsRoute as Any, Icons.Rounded.Settings, stringResource(Res.string.nav_settings)),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (route, icon, label) ->
                    val routeName = route::class.simpleName ?: ""
                    val isSelected = currentEntry?.destination?.route?.contains(routeName) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            tabNavController.navigate(route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            composable<DashboardRoute> {
                DashboardRoot(
                    onNavigateToSubCategory = { id, name ->
                        rootNavController.navigate(SubCategoryRoute(id, name))
                    },
                    onNavigateToAddExpense = { rootNavController.navigate(AddEditExpenseRoute()) }
                )
            }
            composable<AllExpensesRoute> {
                AllExpensesRoot(
                    onNavigateToEdit = { id -> rootNavController.navigate(AddEditExpenseRoute(id)) },
                    onNavigateToAddExpense = { rootNavController.navigate(AddEditExpenseRoute()) }
                )
            }
            composable<AnalyticsRoute> { AnalyticsRoot() }
            composable<SettingsRoute> {
                SettingsRoot(
                    onNavigateToBudget = { rootNavController.navigate(BudgetRoute) },
                    onNavigateToManageCategories = { rootNavController.navigate(ManageCategoriesRoute) },
                    onNavigateToManageSubCategories = { rootNavController.navigate(ManageSubCategoriesRoute) },
                    onNavigateToAppLockSetup = { rootNavController.navigate(AppLockSetupRoute) },
                    onNavigateToNotificationSettings = { rootNavController.navigate(NotificationSettingsRoute) },
                    onNavigateToAbout = { rootNavController.navigate(AboutRoute) },
                    onNavigateToPrivacyPolicy = { rootNavController.navigate(PrivacyPolicyRoute) },
                    onNavigateToTerms = { rootNavController.navigate(TermsOfServiceRoute) },
                    onNavigateToSync = { rootNavController.navigate(SyncRoute) },
                    onNavigateToDownloads = { rootNavController.navigate(DownloadsRoute) },
                    onNavigateToTheme = { rootNavController.navigate(ThemeSettingsRoute) },
                    onNavigateToLanguage = { rootNavController.navigate(LanguageSettingsRoute) }
                )
            }
        }
    }
}
