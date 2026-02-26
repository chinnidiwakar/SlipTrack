package uk.chinnidiwakar.sliptrack

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import uk.chinnidiwakar.sliptrack.utils.DateUtils.formatElapsedTime
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

class SlipTrackWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateViews(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun updateAll(coroutineContext: CoroutineContext, dao: SlipDao, preferenceManager: PreferenceManager) {
            updateNow(SlipTrackApp.instance.applicationContext)
        }

        fun updateNow(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SlipTrackWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                updateViews(context, manager, ids)
            }
        }

        private fun updateViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val dao = DatabaseProvider.get(context).slipDao()
            val prefs = PreferenceManager(context)

            val actualSlip = runBlocking { dao.getLastActualSlip() }
            val journeyName = runBlocking { prefs.journeyName.first() }
            val shields = runBlocking { prefs.streakShields.first() }

            val elapsedText = if (actualSlip == null) {
                "No slip logged"
            } else {
                formatElapsedTime(System.currentTimeMillis() - normalizeTimestamp(actualSlip.timestamp))
            }

            val streakDays = actualSlip?.let { StreakCalculator.fullDaysSince(it.timestamp) } ?: 0

            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                100,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            appWidgetIds.forEach { appWidgetId ->
                val views = RemoteViews(context.packageName, R.layout.sliptrack_widget)
                views.setTextViewText(R.id.widget_streak_days, "$streakDays days clean")
                views.setTextViewText(R.id.widget_elapsed, elapsedText)
                views.setTextViewText(R.id.widget_journey_name, "since $journeyName")
                views.setTextViewText(R.id.widget_shields, "🛡️ $shields")
                views.setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
