package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.domain.InsightsData
import uk.chinnidiwakar.sliptrack.domain.WeeklyReport
import uk.chinnidiwakar.sliptrack.domain.computeInsights
import uk.chinnidiwakar.sliptrack.domain.computeWeeklyReport

class InsightsViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val dao = DatabaseProvider.get(appContext).slipDao()

    private val _insights = MutableStateFlow<InsightsData?>(null)
    val insights: StateFlow<InsightsData?> = _insights.asStateFlow()

    private val _weeklyReport = MutableStateFlow(WeeklyReport(0, 0, 0))
    val weeklyReport: StateFlow<WeeklyReport> = _weeklyReport.asStateFlow()

    init {
        observeInsights()
    }


    suspend fun exportData(uri: Uri): Int {
        val events = dao.getAllSlipsUnordered()
        val json = DataBackupManager.exportToJson(events)
        DataBackupManager.writeToUri(appContext, uri, json)
        return events.size
    }

    suspend fun importData(uri: Uri): Int {
        val json = DataBackupManager.readFromUri(appContext, uri)
        val events = DataBackupManager.parseJson(json)
        dao.clearAll()
        dao.insertAll(events)
        return events.size
    }

    private fun observeInsights() {
        viewModelScope.launch {
            dao.observeAllEvents().collect { events ->
                val slips = events.filter { !it.isResist }
                _insights.value = computeInsights(slips)
                _weeklyReport.value = computeWeeklyReport(events)
            }
        }
    }
}
