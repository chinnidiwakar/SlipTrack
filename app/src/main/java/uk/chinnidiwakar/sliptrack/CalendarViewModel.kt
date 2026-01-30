package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.ui.calendar.CalendarDay
import uk.chinnidiwakar.sliptrack.utils.buildCalendarDays
import java.time.LocalDate

class CalendarViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()



    private val _currentMonth =
        MutableStateFlow(java.time.YearMonth.now())
    val currentMonth: StateFlow<java.time.YearMonth> = _currentMonth

    private val _days = MutableStateFlow<List<CalendarDay>>(emptyList())
    val days: StateFlow<List<CalendarDay>> = _days

    init {
        observeCalendar()
    }

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getRelapseCount(date: LocalDate): Int {
        return _days.value.firstOrNull { it.day == date.dayOfMonth }?.relapses ?: 0
    }


    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    private fun observeCalendar() {
        viewModelScope.launch {
            combine(
                dao.observeAllSlipsUnordered(),
                _currentMonth
            ) { slips, month ->
                buildCalendarDays(slips, month)
            }.collect { calendarDays ->
                _days.value = calendarDays
                _selectedDate.value = null
            }
        }
    }
}
