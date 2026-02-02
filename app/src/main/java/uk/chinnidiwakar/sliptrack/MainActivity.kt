package uk.chinnidiwakar.sliptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import uk.chinnidiwakar.sliptrack.navigation.AppNavigation
import uk.chinnidiwakar.sliptrack.ui.theme.RelapseTrackerTheme
import java.util.concurrent.TimeUnit
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup WorkManager BEFORE setContent
        setupMilestoneWork()

        setContent {
            RelapseTrackerTheme {
                AppNavigation()
            }
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

        // Set to 8:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // If it's already past 8 AM, schedule for tomorrow
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis - now
    }
}