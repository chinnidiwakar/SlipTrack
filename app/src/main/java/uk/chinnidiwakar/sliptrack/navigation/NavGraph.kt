package uk.chinnidiwakar.sliptrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.chinnidiwakar.sliptrack.ui.home.HomeScreen
import uk.chinnidiwakar.sliptrack.ui.history.HistoryScreen
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarScreen
import uk.chinnidiwakar.sliptrack.ui.insights.InsightsScreen
import uk.chinnidiwakar.sliptrack.ui.emergency.EmergencyScreen


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Calendar : Screen("calendar")
    object Insights : Screen("insights")
    object Emergency : Screen("emergency")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Emergency.route) { EmergencyScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = {
                navController.navigate(Screen.History.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            label = { Text("History") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                navController.navigate(Screen.Calendar.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            },
            label = { Text("Calendar") },
            icon = { Icon(Icons.Default.DateRange, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Insights.route,
            onClick = { navController.navigate(Screen.Insights.route) },
            label = { Text("Insights") },
            icon = { Icon(Icons.Default.Analytics, null) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Emergency.route,
            onClick = { navController.navigate(Screen.Emergency.route) },
            label = { Text("SOS") },
            icon = { Icon(Icons.Default.Warning, null) }
        )

    }
}
