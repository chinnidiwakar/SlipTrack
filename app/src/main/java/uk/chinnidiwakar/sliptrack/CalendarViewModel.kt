package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarDay
import uk.chinnidiwakar.sliptrack.utils.buildCalendarDays

class CalendarViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()

    private val _days = MutableStateFlow<List<CalendarDay>>(emptyList())
    val days: StateFlow<List<CalendarDay>> = _days

    init {
        observeCalendar()
    }

    private fun observeCalendar() {
        viewModelScope.launch {
            dao.observeAllSlipsUnordered().collect { slips ->
                _days.value = buildCalendarDays(slips)
            }
        }
    }
}
