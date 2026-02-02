package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.domain.InsightsData
import uk.chinnidiwakar.sliptrack.domain.computeInsights


class InsightsViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()

    private val _insights = MutableStateFlow<InsightsData?>(null)
    val insights: StateFlow<InsightsData?> = _insights

    init {
        observeInsights()
    }

    private fun observeInsights() {
        viewModelScope.launch {
            dao.observeSlipsOnly().collect { slips ->
            _insights.value = computeInsights(slips)
            }
        }
    }
}
