package com.example.spendsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spendsense.ui.screens.*
import com.example.spendsense.ui.theme.SpendsenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var currency by remember { mutableStateOf("$") }

            // Conversion rates relative to USD ($) - Base Currency is USD
            val conversionRates = mapOf(
                "$" to 1.0,
                "€" to 0.92,
                "£" to 0.79,
                "₹" to 83.33,
                "¥" to 151.0
            )

            val currentRate = conversionRates[currency] ?: 1.0

            SpendsenseTheme(darkTheme = isDarkMode) {
                MainApp(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it },
                    currency = currency,
                    onCurrencyChange = { currency = it },
                    rate = currentRate
                )
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.History)
    object Analytics : Screen("analytics", "Insights", Icons.Default.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainApp(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    rate: Double
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.Analytics,
        Screen.Profile,
        Screen.Settings
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    currency = currency,
                    rate = rate,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                    onNavigateToAnalytics = { navController.navigate("analytics") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("history") {
                TransactionHistoryScreen(
                    currency = currency,
                    rate = rate,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("analytics") {
                AnalyticsScreen(
                    currency = currency,
                    rate = rate,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    currency = currency,
                    onCurrencyChange = onCurrencyChange,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
