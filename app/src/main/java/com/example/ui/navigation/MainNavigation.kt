package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddEditPurchaseScreen
import com.example.ui.screens.AskPurchasesScreen
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.MyStuffScreen
import com.example.ui.screens.OcrReviewScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.PurchaseDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object MyStuff : Screen("my_stuff", "My Stuff", Icons.Default.ShoppingBag)
    data object Insights : Screen("insights", "Insights", Icons.Default.Analytics)
    data object Documents : Screen("documents", "Documents", Icons.Default.Folder)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object PurchaseDetail : Screen("purchase_detail/{id}", "Details") {
        fun createRoute(id: Long) = "purchase_detail/$id"
    }
    data object AddEditPurchase : Screen("add_edit_purchase?id={id}", "Add Purchase") {
        fun createRoute(id: Long? = null) = if (id != null) "add_edit_purchase?id=$id" else "add_edit_purchase"
    }
    data object OcrReview : Screen("ocr_review", "Review Purchase")
    data object AskAi : Screen("ask_ai", "Ask My Purchases")
    data object Premium : Screen("premium", "Premium")
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.MyStuff,
        Screen.Insights,
        Screen.Documents,
        Screen.Settings
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            modifier = Modifier.testTag("nav_item_${screen.route}"),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPurchaseDetail = { id ->
                        navController.navigate(Screen.PurchaseDetail.createRoute(id))
                    },
                    onNavigateToAddPurchase = {
                        navController.navigate(Screen.AddEditPurchase.createRoute())
                    },
                    onNavigateToOcrReview = {
                        navController.navigate(Screen.OcrReview.route)
                    },
                    onNavigateToAskAi = {
                        navController.navigate(Screen.AskAi.route)
                    },
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
                    },
                    onNavigateToMyStuff = {
                        navController.navigate(Screen.MyStuff.route)
                    }
                )
            }

            composable(Screen.MyStuff.route) {
                MyStuffScreen(
                    viewModel = viewModel,
                    onNavigateToPurchaseDetail = { id ->
                        navController.navigate(Screen.PurchaseDetail.createRoute(id))
                    },
                    onNavigateToAddPurchase = {
                        navController.navigate(Screen.AddEditPurchase.createRoute())
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(viewModel = viewModel)
            }

            composable(Screen.Documents.route) {
                DocumentsScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
                    }
                )
            }

            composable(
                route = Screen.PurchaseDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                PurchaseDetailScreen(
                    purchaseId = id,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId ->
                        navController.navigate(Screen.AddEditPurchase.createRoute(editId))
                    }
                )
            }

            composable(
                route = Screen.AddEditPurchase.route,
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id")
                AddEditPurchaseScreen(
                    purchaseId = if (id != null && id > 0) id else null,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                )
            }

            composable(Screen.OcrReview.route) {
                OcrReviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                )
            }

            composable(Screen.AskAi.route) {
                AskPurchasesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Premium.route) {
                PremiumScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
