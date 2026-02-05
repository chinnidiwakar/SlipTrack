package uk.chinnidiwakar.sliptrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class StreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dao = DatabaseProvider.get(applicationContext).slipDao()

    override suspend fun doWork(): Result {
        val lastSlip = dao.getLastActualSlip() ?: return Result.success()
        val streakDays = daysSince(lastSlip.timestamp)

        val milestones = setOf(1, 3, 7, 14, 30, 60, 90)
        if (streakDays in milestones) {
            createNotificationChannel()
            postMilestoneNotification(streakDays)
        }

        return Result.success()
    }

    private fun daysSince(timestamp: Long): Int {
        val zone = ZoneId.systemDefault()
        val lastDate = Instant.ofEpochMilli(normalizeTimestamp(timestamp)).atZone(zone).toLocalDate()
        return ChronoUnit.DAYS.between(lastDate, LocalDate.now()).toInt()
    }

    private fun normalizeTimestamp(raw: Long): Long {
        return if (raw < 1_000_000_000_000L) raw * 1000 else raw
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
            7 -> "One full week! Proud of you 🏆"
            else -> "$streakDays-day streak. Keep building 💪"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Milestone unlocked")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(streakDays, notification)
    }

    companion object {
        private const val CHANNEL_ID = "streak_milestones"
    }
}
