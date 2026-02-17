package uk.chinnidiwakar.sliptrack.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import uk.chinnidiwakar.sliptrack.ui.home.HomeScreen
import uk.chinnidiwakar.sliptrack.ui.insights.InsightsScreen
import uk.chinnidiwakar.sliptrack.ui.settings.SettingsScreen


sealed class Screen(val route: String) {
    object Home : Screen("home")
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(Screen.Home.route) { HomeScreen(navController = navController) }
                composable(Screen.Calendar.route) { CalendarScreen() }
                composable(Screen.Insights.route) { InsightsScreen() }
                composable(Screen.Emergency.route) { EmergencyScreen(onClose = { navController.popBackStack() }) }
                composable(Screen.Settings.route) {
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
                        viewModel = homeViewModel,
                        onExport = { exportLauncher.launch("sliptrack-backup.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json")) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // 👇 Overlay bottom nav manually
            BottomNavigationBar(navController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    if (currentRoute == Screen.Emergency.route) return

    Box(
        modifier = Modifier
            .fillMaxSize(),   // 👈 THIS was missing
        contentAlignment = Alignment.BottomCenter
    ) {

        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
                .height(62.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black.copy(alpha = 0.35f))
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                FrostedNavItem(
                    selected = currentRoute == Screen.Home.route ||
                            currentRoute == Screen.Emergency.route ||
                            currentRoute == Screen.Settings.route,
                    icon = Icons.Default.Home
                ) {
                    if (currentRoute != Screen.Home.route &&
                        currentRoute != Screen.Emergency.route &&
                        currentRoute != Screen.Settings.route) {

                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                }

                FrostedNavItem(
                    selected = currentRoute == Screen.Calendar.route,
                    icon = Icons.Default.DateRange
                ) {
                    if (currentRoute != Screen.Calendar.route) {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                FrostedNavItem(
                    selected = currentRoute == Screen.Insights.route,
                    icon = Icons.Default.Analytics
                ) {
                    if (currentRoute != Screen.Insights.route) {
                        navController.navigate(Screen.Insights.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FrostedNavItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(250),
        label = "navScale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (selected)
                    Color.White.copy(alpha = 0.15f)
                else
                    Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected)
                Color.White
            else
                Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
    }
}
