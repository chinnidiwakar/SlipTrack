package uk.chinnidiwakar.sliptrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.utils.formatElapsedTime

class HomeViewModel(
    private val dao: SlipDao
) : ViewModel() {

    private val _dailyQuote = MutableStateFlow("")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    private val _elapsedText = MutableStateFlow("0m")
    val elapsedText: StateFlow<String> = _elapsedText

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak

    private val _uiMessages = MutableSharedFlow<String>()
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    private var lastRelapseTime = System.currentTimeMillis()

    init {
        _dailyQuote.value = QuoteRepository.getQuoteForToday()
        observeSlips()
        startTimer()
    }

    private fun observeSlips() {
        viewModelScope.launch {
            dao.observeAllSlips().collect { allEvents ->
                val actualSlips = allEvents.filter { !it.isResist }

                val baselineTime = when {
                    actualSlips.isNotEmpty() -> actualSlips.maxBy { it.timestamp }.timestamp
                    allEvents.isNotEmpty() -> allEvents.minBy { it.timestamp }.timestamp
                    else -> null
                }

                baselineTime?.let { lastRelapseTime = normalizeTimestamp(it) }

                val current = if (actualSlips.isNotEmpty()) {
                    StreakCalculator.currentStreak(actualSlips)
                } else {
                    baselineTime?.let { daysSince(it) } ?: 0
                }
                val longest = if (actualSlips.isNotEmpty()) {
                    StreakCalculator.longestStreak(actualSlips)
                } else {
                    current
                }

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

    private fun normalizeTimestamp(raw: Long): Long {
        return if (raw < 1_000_000_000_000L) raw * 1000 else raw
    }

    private fun daysSince(rawTimestamp: Long): Int {
        val zone = java.time.ZoneId.systemDefault()
        val date = java.time.Instant.ofEpochMilli(normalizeTimestamp(rawTimestamp))
            .atZone(zone)
            .toLocalDate()
        return java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now()).toInt()
    }

    fun logSlip() {
        viewModelScope.launch {
            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = false
                )
            )
        }
    }

    fun logEvent(isResist: Boolean, intensity: Int = 0) {
        viewModelScope.launch {
            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = isResist,
                    intensity = intensity,
                    trigger = trigger
                )
            )

            if (isResist) {
                val msg = when (intensity) {
                    1 -> "🌱 Spark extinguished! Good catch."
                    2 -> "⚔️ Stayed strong through the urge!"
                    3 -> "🏆 MASSIVE VICTORY! You conquered the pit."
                    else -> "Victory logged!"
                }
                _uiMessages.emit(msg)
            }
        }
    }
}
