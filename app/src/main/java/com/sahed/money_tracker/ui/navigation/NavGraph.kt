package com.sahed.money_tracker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahed.money_tracker.ui.components.MoneyBottomBar
import com.sahed.money_tracker.ui.designsystem.theme.BottomSheetShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.ui.screens.auth.LoginScreen
import com.sahed.money_tracker.ui.screens.dashboard.DashboardScreen
import com.sahed.money_tracker.ui.screens.entry.AddEntryScreen
import com.sahed.money_tracker.ui.screens.entry.EntriesScreen
import com.sahed.money_tracker.ui.screens.onboarding.OnboardingScreen
import com.sahed.money_tracker.ui.screens.settings.ManageSourcesScreen
import com.sahed.money_tracker.ui.screens.settings.SettingsScreen
import com.sahed.money_tracker.ui.screens.statistics.AllTimeStatisticsScreen
import com.sahed.money_tracker.viewmodel.AddEntryViewModel
import com.sahed.money_tracker.viewmodel.AllTimeStatisticsViewModel
import com.sahed.money_tracker.viewmodel.AuthUiState
import com.sahed.money_tracker.viewmodel.AuthViewModel
import com.sahed.money_tracker.viewmodel.DashboardViewModel
import com.sahed.money_tracker.viewmodel.EntriesViewModel
import com.sahed.money_tracker.viewmodel.ManageSourcesViewModel
import com.sahed.money_tracker.viewmodel.OnboardingViewModel
import com.sahed.money_tracker.viewmodel.SettingsViewModel

object AppRoutes {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val ENTRIES = "entries"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val MANAGE_SOURCES = "manage_sources"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()

    // Render smooth loading screen while checking initial session
    if (authState is AuthUiState.Initial) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(EmeraldTheme.extended.surfaceTier1),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EmeraldPalette.SoftEmerald)
        }
        return
    }

    // Add Entry Sheet Visibility State (can be opened from anywhere via center "+")
    var showAddEntrySheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.LOGIN

    val showBottomBar = currentRoute in setOf(
        AppRoutes.DASHBOARD,
        AppRoutes.ENTRIES,
        AppRoutes.STATISTICS,
        AppRoutes.SETTINGS
    )

    // Enforce mandatory login: if unauthenticated at any point, navigate to welcome/login screen
    androidx.compose.runtime.LaunchedEffect(authState) {
        if (authState is AuthUiState.Unauthenticated) {
            if (currentRoute != AppRoutes.LOGIN) {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

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
                        showAddEntrySheet = false
                        if (currentRoute != AppRoutes.DASHBOARD) {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToEntries = {
                        showAddEntrySheet = false
                        if (currentRoute != AppRoutes.ENTRIES) {
                            navController.navigate(AppRoutes.ENTRIES) {
                                popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToStatistics = {
                        showAddEntrySheet = false
                        if (currentRoute != AppRoutes.STATISTICS) {
                            navController.navigate(AppRoutes.STATISTICS) {
                                popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToSettings = {
                        showAddEntrySheet = false
                        if (currentRoute != AppRoutes.SETTINGS) {
                            navController.navigate(AppRoutes.SETTINGS) {
                                popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onOpenAddEntry = {
                        showAddEntrySheet = !showAddEntrySheet
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.985f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.985f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                    }
                ) {
                    composable(AppRoutes.LOGIN) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToDashboard = {
                                navController.navigate(AppRoutes.DASHBOARD) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToOnboarding = {
                                navController.navigate(AppRoutes.ONBOARDING) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
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
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(AppRoutes.DASHBOARD) {
                        val dashboardViewModel: DashboardViewModel = viewModel()
                        DashboardScreen(viewModel = dashboardViewModel)
                    }

                    composable(AppRoutes.ENTRIES) {
                        val entriesViewModel: EntriesViewModel = viewModel()
                        EntriesScreen(viewModel = entriesViewModel)
                    }

                    composable(AppRoutes.STATISTICS) {
                        val statisticsViewModel: AllTimeStatisticsViewModel = viewModel()
                        AllTimeStatisticsScreen(viewModel = statisticsViewModel)
                    }

                    composable(AppRoutes.SETTINGS) {
                        val settingsViewModel: SettingsViewModel = viewModel()
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateToManageSources = {
                                navController.navigate(AppRoutes.MANAGE_SOURCES)
                            },
                            onSignOut = {
                                authViewModel.signOut()
                                navController.navigate(AppRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(
                        route = AppRoutes.MANAGE_SOURCES,
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(280))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(240))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(280))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(240))
                        }
                    ) {
                        val manageSourcesViewModel: ManageSourcesViewModel = viewModel()
                        ManageSourcesScreen(
                            viewModel = manageSourcesViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            // Swipeable Add Entry Modal Bottom Sheet when center "+" is pressed from anywhere
            if (showAddEntrySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddEntrySheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    shape = BottomSheetShape,
                    containerColor = EmeraldTheme.extended.surfaceTier2,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(EmeraldTheme.extended.subText.copy(alpha = 0.4f))
                        )
                    }
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
}
