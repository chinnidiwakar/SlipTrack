package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // This is where we will check milestones and send notifications
        // We'll implement the logic here once we finish the UI
        return Result.success()
    }
}