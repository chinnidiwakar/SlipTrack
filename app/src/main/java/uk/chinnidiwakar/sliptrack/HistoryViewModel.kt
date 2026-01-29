package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.ui.history.DaySummary
import uk.chinnidiwakar.sliptrack.utils.buildDaySummaries


class HistoryViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()

    private val _history = MutableStateFlow<List<DaySummary>>(emptyList())
    val history: StateFlow<List<DaySummary>> = _history

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            dao.observeAllSlips().collect { slips ->
                _history.value = buildDaySummaries(slips)
            }
        }
    }
}
