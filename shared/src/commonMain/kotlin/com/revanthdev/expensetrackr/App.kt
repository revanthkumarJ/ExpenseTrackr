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
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.nav_analytics
import expensetrackr.core.presentation.generated.resources.nav_dashboard
import expensetrackr.core.presentation.generated.resources.nav_expenses
import expensetrackr.core.presentation.generated.resources.nav_settings
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

@Composable
fun App() {
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
            AppNavHost(startDestination = startDestination!!)
        }
    }
}

@Composable
private fun AppNavHost(startDestination: Any) {
    val rootNavController = rememberNavController()

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
            })
        }

        composable<AppLockRoute> {
            AppLockRoot(
                onUnlocked = {
                    rootNavController.navigate(MainRoute) {
                        popUpTo(AppLockRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<MainRoute> { MainScaffold(rootNavController = rootNavController) }

        composable<AddEditExpenseRoute> {
            AddEditExpenseRoot(onNavigateBack = { rootNavController.popBackStack() })
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
                    onNavigateToTerms = { rootNavController.navigate(TermsOfServiceRoute) }
                )
            }
        }
    }
}
