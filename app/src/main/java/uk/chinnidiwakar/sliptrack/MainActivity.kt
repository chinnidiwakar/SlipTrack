package uk.chinnidiwakar.sliptrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.navigation.AppNavigation
import uk.chinnidiwakar.sliptrack.ui.theme.RelapseTrackerTheme
import uk.chinnidiwakar.sliptrack.wear.WearStatePayload
import uk.chinnidiwakar.sliptrack.wear.WearSyncClient
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op; milestones will begin when permission is granted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        setupMilestoneWork()
        startWearSync()
        enableEdgeToEdge()
        setContent {
            RelapseTrackerTheme {
                AppNavigation()
            }
        }
    }

    private fun startWearSync() {
        val dao = DatabaseProvider.get(this).slipDao()
        val prefs = PreferenceManager(this)
        val syncClient = WearSyncClient(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(dao.observeAllSlips(), prefs.shieldCharges) { allEvents, shieldCharges ->
                    val slips = allEvents.filter { !it.isResist }
                    WearStatePayload(
                        currentStreak = if (slips.isEmpty()) 0 else StreakCalculator.currentStreak(slips),
                        longestStreak = if (slips.isEmpty()) 0 else StreakCalculator.longestStreak(slips),
                        shieldCharges = shieldCharges,
                        updatedAt = System.currentTimeMillis()
                    )
                }.collect { payload ->
                    runCatching { syncClient.pushState(payload) }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupMilestoneWork() {
        val streakRequest = PeriodicWorkRequestBuilder<StreakWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntilMorning(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MilestoneCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            streakRequest
        )
    }

    private fun calculateDelayUntilMorning(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis - now
    }
}
