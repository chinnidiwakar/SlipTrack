package uk.chinnidiwakar.sliptrack

import android.content.Context
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

    private val dao = DatabaseProvider.get(context).slipDao()

    private val _insights = MutableStateFlow<InsightsData?>(null)
    val insights: StateFlow<InsightsData?> = _insights.asStateFlow()

    private val _weeklyReport = MutableStateFlow(WeeklyReport(0, 0, 0))
    val weeklyReport: StateFlow<WeeklyReport> = _weeklyReport.asStateFlow()

    init {
        observeInsights()
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
