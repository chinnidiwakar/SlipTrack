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
import uk.chinnidiwakar.sliptrack.ui.calendar.DaySummary
import uk.chinnidiwakar.sliptrack.utils.DateUtils.buildCalendarDays
import uk.chinnidiwakar.sliptrack.utils.DateUtils.buildDaySummaries
import java.time.LocalDate
import java.time.YearMonth

class HistoryViewModel(private val dao: SlipDao) : ViewModel() {
    private val _history = MutableStateFlow<List<DaySummary>>(emptyList())
    val history: StateFlow<List<DaySummary>> = _history

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            // Using the reactive flow from your SlipDao
            dao.observeAllSlips().collect { slips ->
                _history.value = buildDaySummaries(slips)
            }
        }
    }
}

class CalendarViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()

    // ---------- STATE ----------

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _days = MutableStateFlow<List<CalendarDay>>(emptyList())
    val days: StateFlow<List<CalendarDay>> = _days.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    // ---------- CACHE (IMPORTANT) ----------

    private var cachedSlips = emptyList<SlipEvent>()
    private var lastEmittedMonth: YearMonth? = null

    // ---------- INIT ----------

    init {
        observeCalendar()
    }

    // ---------- UI ACTIONS ----------

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getDayData(date: LocalDate): CalendarDay? {
        if (YearMonth.from(date) != _currentMonth.value) return null
        return _days.value.firstOrNull { it.day == date.dayOfMonth }
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    // ---------- READ HELPERS ----------

    fun getRelapseCount(date: LocalDate): Int {
        // Only check if the date belongs to the month currently in view
        if (YearMonth.from(date) != _currentMonth.value) return 0

        return _days.value
            .firstOrNull { it.day == date.dayOfMonth }
            ?.relapses ?: 0
    }


    /**
     * Used by Pager for prev / next month.
     * NO database access here.
     */
    fun getDaysForMonth(month: YearMonth): List<CalendarDay> {
        return buildCalendarDays(
            slips = cachedSlips,
            month = month
        )
    }

    // ---------- INTERNAL ----------
    private fun observeCalendar() {
        viewModelScope.launch {
            combine(
                dao.observeAllSlipsUnordered(),
                _currentMonth
            ) { slips, month ->
                cachedSlips = slips
                month to buildCalendarDays(slips, month)
            }.collect { (month, calendarDays) ->

                // FIX: Don't set to null. Set to the 1st of the month instead.
                if (lastEmittedMonth != month) {
                    // If the new month is the current real-world month, pick 'today'
                    // Otherwise, pick the 1st of that month.
                    _selectedDate.value = if (month == YearMonth.now()) {
                        LocalDate.now()
                    } else {
                        month.atDay(1)
                    }
                    lastEmittedMonth = month
                }

                _days.value = calendarDays
            }
        }
    }
}