package uk.chinnidiwakar.sliptrack.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.DatabaseProvider
import uk.chinnidiwakar.sliptrack.HomeViewModel
import uk.chinnidiwakar.sliptrack.HomeViewModelFactory
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory
import uk.chinnidiwakar.sliptrack.PreferenceManager
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarScreen
import uk.chinnidiwakar.sliptrack.ui.emergency.EmergencyScreen
import uk.chinnidiwakar.sliptrack.ui.history.HistoryScreen
import uk.chinnidiwakar.sliptrack.ui.home.HomeScreen
import uk.chinnidiwakar.sliptrack.ui.insights.InsightsScreen
import uk.chinnidiwakar.sliptrack.ui.settings.SettingsScreen


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Calendar : Screen("calendar")
    object Insights : Screen("insights")
    object Emergency : Screen("emergency")
    object Settings : Screen("settings")
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Get the database instance first
    val db = remember { DatabaseProvider.get(context) }

    // 2. Extract the DAO from the database
    val dao = remember { db.slipDao() }

    val preferenceManager = remember { PreferenceManager(context) }

    // 3. Pass the DAO (not the whole DB) to the factory
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(dao, preferenceManager)
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Emergency.route) { EmergencyScreen(onClose = { navController.popBackStack() }) }
            composable(Screen.Settings.route) {
                // 'context' is already defined at the top of AppNavigation,
                // so we don't need to re-declare it here unless you want a local one.
                val scope = rememberCoroutineScope()

                val insightsViewModel: InsightsViewModel = viewModel(
                    factory = InsightsViewModelFactory(context)
                )

                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    if (uri != null) {
                        scope.launch { insightsViewModel.exportData(uri) }
                    }
                }

                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        scope.launch { insightsViewModel.importData(uri) }
                    }
                }

                SettingsScreen(
                    viewModel = homeViewModel, // Matches the variable at the top now!
                    onExport = { exportLauncher.launch("sliptrack-backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json")) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route ||
                    currentRoute == Screen.Emergency.route ||
                    currentRoute == Screen.Settings.route,
            onClick = {
                // 1. If we are on a completely different tab (History/Calendar/Insights)
                if (currentRoute != Screen.Home.route &&
                    currentRoute != Screen.Emergency.route &&
                    currentRoute != Screen.Settings.route) {

                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    // 2. If we are ALREADY in the Home "territory" (Home, SOS, or Settings)
                    // This force-clears everything back to the actual Home Screen
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            },
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = {
                if (currentRoute != Screen.History.route) { // 👈 Prevents reloading if already there
                    navController.navigate(Screen.History.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("History") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                if (currentRoute != Screen.Calendar.route) { // 👈 Prevents reloading if already there
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("Calendar") },
            icon = { Icon(Icons.Default.DateRange, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Insights.route,
            onClick = {
                if (currentRoute != Screen.Insights.route) { // 👈 Prevents reloading if already there
                    navController.navigate(Screen.Insights.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("Insights") },
            icon = { Icon(Icons.Default.Analytics, null) }
        )
    }
}
