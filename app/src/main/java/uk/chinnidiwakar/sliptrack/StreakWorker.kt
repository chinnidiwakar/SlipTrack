package uk.chinnidiwakar.sliptrack

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class StreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dao = DatabaseProvider.get(applicationContext).slipDao()
    private val preferenceManager = PreferenceManager(applicationContext)

    // Inside StreakWorker.kt

    override suspend fun doWork(): Result {
        val lastSlip = dao.getLastActualSlip() ?: return Result.success()

        // Unified 24-hour math
        val diffMillis = System.currentTimeMillis() - lastSlip.timestamp
        val streakDays = (diffMillis / (1000L * 60 * 60 * 24)).toInt()

        // Get the last milestone we actually awarded from Prefs
        val lastAwarded = preferenceManager.getLastAwardedMilestone().first()

        // Calculate how many NEW milestone shields to give
        val shieldsToAward = ShieldPolicy.countNewMilestoneShields(lastAwarded, streakDays)

        if (shieldsToAward > 0) {
            // 1. Update the last awarded milestone to the current highest one
            val currentHighest = ShieldPolicy.highestReachedMilestone(streakDays)
            preferenceManager.saveLastAwardedMilestone(currentHighest)

            // 2. Add the shields to the user's total
            preferenceManager.addShields(shieldsToAward)

            // 3. Notify the user
            createNotificationChannel()
            postMilestoneNotification(streakDays, shieldsToAward)

            // 4. Refresh the Widget immediately
            //SlipTrackWidgetReceiver.refresh(applicationContext)
        }

        return Result.success()
    }

    private fun daysSince(timestamp: Long): Int {
        val diff = System.currentTimeMillis() - timestamp
        return (diff / (1000L * 60 * 60 * 24)).toInt()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Streak Milestones",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Celebrates recovery milestones"
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun postMilestoneNotification(streakDays: Int, shieldsAwarded: Int) {
        val baseMessage = when (streakDays) {
            1 -> "Day 1 complete. Keep going 🌱"
            3 -> "3-day streak! Solid momentum ⚡"
            else -> "$streakDays-day streak. Keep building 💪"
        }

        val rewardSuffix = if (shieldsAwarded > 0) {
            " You earned $shieldsAwarded shield${if (shieldsAwarded > 1) "s" else ""} 🛡️"
        } else {
            ""
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Milestone unlocked")
            .setContentText(baseMessage + rewardSuffix)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(applicationContext).notify(streakDays, notification)
    }

    companion object {
        private const val CHANNEL_ID = "streak_milestones"
    }
}
