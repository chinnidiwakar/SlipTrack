package uk.chinnidiwakar.sliptrack

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SlipTrackWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, SlipTrackWidgetProvider::class.java))
            ids.forEach { id -> manager.updateAppWidget(id, buildRemoteViews(context)) }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.sliptrack_widget)

            val stats = runBlocking {
                val dao = DatabaseProvider.get(context).slipDao()
                val allEvents = dao.getAllSlipsUnordered()
                val slips = allEvents.filter { !it.isResist }
                val currentStreak = if (slips.isNotEmpty()) StreakCalculator.currentStreak(slips) else 0
                val latestSlip = dao.getLastActualSlip()?.timestamp
                val journey = PreferenceManager(context).journeyName.first()
                Triple(currentStreak, latestSlip, journey)
            }

            views.setTextViewText(R.id.widgetTitle, "SlipTrack • ${stats.third}")
            views.setTextViewText(R.id.widgetStreak, "${stats.first} day streak")
            val subtitle = stats.second?.let {
                val elapsed = uk.chinnidiwakar.sliptrack.utils.DateUtils.formatElapsedTime(System.currentTimeMillis() - it)
                "Last slip: $elapsed ago"
            } ?: "No slip logged yet"
            views.setTextViewText(R.id.widgetSubtitle, subtitle)
            return views
        }
    }
}
