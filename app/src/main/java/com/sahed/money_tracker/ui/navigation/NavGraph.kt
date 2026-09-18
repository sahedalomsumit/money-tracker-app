package com.sahed.money_tracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahed.money_tracker.ui.components.MoneyBottomBar
import com.sahed.money_tracker.ui.screens.auth.LoginScreen
import com.sahed.money_tracker.ui.screens.dashboard.DashboardScreen
import com.sahed.money_tracker.ui.screens.entry.AddEntryScreen
import com.sahed.money_tracker.ui.screens.onboarding.OnboardingScreen
import com.sahed.money_tracker.ui.screens.settings.ManageSourcesScreen
import com.sahed.money_tracker.ui.screens.settings.SettingsScreen
import com.sahed.money_tracker.viewmodel.AddEntryViewModel
import com.sahed.money_tracker.viewmodel.AuthUiState
import com.sahed.money_tracker.viewmodel.AuthViewModel
import com.sahed.money_tracker.viewmodel.DashboardViewModel
import com.sahed.money_tracker.viewmodel.ManageSourcesViewModel
import com.sahed.money_tracker.viewmodel.OnboardingViewModel
import com.sahed.money_tracker.viewmodel.SettingsViewModel

object AppRoutes {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val MANAGE_SOURCES = "manage_sources"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()

    // Add Entry Sheet Visibility State (can be opened from anywhere via center "+")
    var showAddEntrySheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.LOGIN

    val showBottomBar = currentRoute == AppRoutes.DASHBOARD || currentRoute == AppRoutes.SETTINGS

    // Determine initial route based on session persistence
    val startDestination = when (val state = authState) {
        is AuthUiState.Authenticated -> {
            if (state.isOnboarded) AppRoutes.DASHBOARD else AppRoutes.ONBOARDING
        }
        else -> AppRoutes.LOGIN
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MoneyBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToDashboard = {
                        if (currentRoute != AppRoutes.DASHBOARD) {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToSettings = {
                        if (currentRoute != AppRoutes.SETTINGS) {
                            navController.navigate(AppRoutes.SETTINGS)
                        }
                    },
                    onOpenAddEntry = {
                        showAddEntrySheet = true
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppRoutes.LOGIN) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToDashboard = {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToOnboarding = {
                            navController.navigate(AppRoutes.ONBOARDING) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(AppRoutes.ONBOARDING) {
                    val onboardingViewModel: OnboardingViewModel = viewModel()
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onOnboardingComplete = {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                composable(AppRoutes.DASHBOARD) {
                    val dashboardViewModel: DashboardViewModel = viewModel()
                    DashboardScreen(viewModel = dashboardViewModel)
                }

                composable(AppRoutes.SETTINGS) {
                    val settingsViewModel: SettingsViewModel = viewModel()
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToManageSources = {
                            navController.navigate(AppRoutes.MANAGE_SOURCES)
                        },
                        onSignOut = {
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(AppRoutes.MANAGE_SOURCES) {
                    val manageSourcesViewModel: ManageSourcesViewModel = viewModel()
                    ManageSourcesScreen(
                        viewModel = manageSourcesViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Fullscreen Add Entry Overlay when center "+" is pressed from anywhere
            AnimatedVisibility(
                visible = showAddEntrySheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                val addEntryViewModel: AddEntryViewModel = viewModel()
                AddEntryScreen(
                    viewModel = addEntryViewModel,
                    onEntrySaved = {
                        showAddEntrySheet = false
                    },
                    onDismiss = {
                        showAddEntrySheet = false
                    }
                )
            }
        }
    }
}
