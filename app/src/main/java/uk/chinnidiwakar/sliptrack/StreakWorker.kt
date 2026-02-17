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
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import uk.chinnidiwakar.sliptrack.utils.toLocalDate

class StreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dao = DatabaseProvider.get(applicationContext).slipDao()
    private val preferenceManager = PreferenceManager(applicationContext)

    override suspend fun doWork(): Result {
        val lastSlip = dao.getLastActualSlip() ?: return Result.success()
        val streakDays = daysSince(lastSlip.timestamp)

        val milestones = setOf(1, 3, 7, 14, 30, 60, 90)
        if (streakDays in milestones) {
            if (streakDays >= 7) {
                preferenceManager.awardShield()
            }
            createNotificationChannel()
            postMilestoneNotification(streakDays)
        }

        return Result.success()
    }

    private fun daysSince(timestamp: Long): Int {
        val lastDate = toLocalDate(timestamp)
        return ChronoUnit.DAYS.between(lastDate, LocalDate.now()).toInt()
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

    private fun postMilestoneNotification(streakDays: Int) {
        val message = when (streakDays) {
            1 -> "Day 1 complete. Keep going 🌱"
            3 -> "3-day streak! Solid momentum ⚡"
            7 -> "One full week! You earned a streak shield 🛡️"
            14 -> "2 weeks strong! Bonus shield unlocked 🛡️"
            30 -> "30-day legend. Another shield added ✨"
            else -> "$streakDays-day streak. Keep building 💪"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Milestone unlocked")
            .setContentText(message)
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
