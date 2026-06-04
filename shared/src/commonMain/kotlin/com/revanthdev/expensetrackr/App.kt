package com.revanthdev.expensetrackr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.feature.analytics.presentation.AnalyticsRoot
import com.revanthdev.expensetrackr.feature.analytics.presentation.AnalyticsRoute
import com.revanthdev.expensetrackr.feature.applock.presentation.AppLockRoot
import com.revanthdev.expensetrackr.feature.applock.presentation.AppLockRoute
import com.revanthdev.expensetrackr.feature.budget.presentation.BudgetRoot
import com.revanthdev.expensetrackr.feature.budget.presentation.BudgetRoute
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageCategoriesRoot
import com.revanthdev.expensetrackr.feature.categories.presentation.ManageCategoriesRoute
import com.revanthdev.expensetrackr.feature.dashboard.presentation.DashboardRoot
import com.revanthdev.expensetrackr.feature.dashboard.presentation.DashboardRoute
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

    ExpenseTrackerTheme(darkTheme = settingsState?.isDarkMode ?: false) {
        if (startDestination == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ExpenseTrackerTheme
        }
        AppNavHost(startDestination = startDestination!!)
    }
}

@Composable
private fun AppNavHost(startDestination: Any) {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = startDestination) {
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
                },
                onTriggerBiometric = {}
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
        Triple(DashboardRoute as Any, Icons.Rounded.PieChart, "Dashboard"),
        Triple(AllExpensesRoute as Any, Icons.Rounded.Receipt, "Expenses"),
        Triple(AnalyticsRoute as Any, Icons.Rounded.BarChart, "Analytics"),
        Triple(SettingsRoute as Any, Icons.Rounded.Settings, "Settings"),
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
        NavHost(navController = tabNavController, startDestination = DashboardRoute, modifier = Modifier.padding(padding)) {
            composable<DashboardRoute> {
                DashboardRoot(
                    onNavigateToSubCategory = { _, _ -> },
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
                    onNavigateToManageSubCategories = { rootNavController.navigate(ManageCategoriesRoute) },
                    onNavigateToAppLockSetup = {},
                    onNavigateToNotificationSettings = {},
                    onNavigateToAbout = { rootNavController.navigate(AboutRoute) },
                    onNavigateToPrivacyPolicy = { rootNavController.navigate(PrivacyPolicyRoute) },
                    onNavigateToTerms = { rootNavController.navigate(TermsOfServiceRoute) }
                )
            }
        }
    }
}
