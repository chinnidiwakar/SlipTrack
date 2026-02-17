package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MilestoneWorkScheduler {

    fun schedule(context: Context) {
        val streakRequest = PeriodicWorkRequestBuilder<StreakWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntilMorning(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
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
