package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uk.chinnidiwakar.sliptrack.utils.formatElapsedTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.asStateFlow
import uk.chinnidiwakar.sliptrack.QuoteRepository

class HomeViewModel(context: Context) : ViewModel() {

    private val dao = DatabaseProvider.get(context).slipDao()

    private val _dailyQuote = MutableStateFlow("")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    private val _elapsedText = MutableStateFlow("0m")
    val elapsedText: StateFlow<String> = _elapsedText

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak

    private var lastRelapseTime = System.currentTimeMillis()

    init {
        _dailyQuote.value = QuoteRepository.getQuoteForToday()
        observeSlips()
        startTimer()
    }

    private fun observeSlips() {
        viewModelScope.launch {
            dao.observeAllSlips().collect { slips ->

                if (slips.isNotEmpty()) {
                    val raw = slips.maxBy { it.timestamp }.timestamp
                    lastRelapseTime = if (raw < 1_000_000_000_000L) raw * 1000 else raw

                }

                val current = StreakCalculator.currentStreak(slips)
                val longest = StreakCalculator.longestStreak(slips)

                _currentStreak.value = current
                _longestStreak.value = maxOf(current, longest)
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                _elapsedText.value =
                    formatElapsedTime(
                        System.currentTimeMillis() - lastRelapseTime
                    )
                delay(1000)
            }
        }
    }

    fun logSlip() {
        viewModelScope.launch {
            dao.insertSlip(SlipEvent(timestamp = System.currentTimeMillis()))
        }
    }
}
