package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.widget.Toast
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

class HomeViewModel(private val context: Context) : ViewModel() {

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

    // Inside HomeViewModel.kt
    private fun observeSlips() {
        viewModelScope.launch {
            dao.observeAllSlips().collect { allEvents ->
                // 1. FILTER HERE: Only look at actual slips (isResist == false)
                val actualSlips = allEvents.filter { !it.isResist }

                val baselineTime = when {
                    actualSlips.isNotEmpty() -> actualSlips.maxBy { it.timestamp }.timestamp
                    allEvents.isNotEmpty() -> allEvents.minBy { it.timestamp }.timestamp
                    else -> null
                }

                baselineTime?.let { lastRelapseTime = normalizeTimestamp(it) }

                // 2. Pass the filtered list to the calculator for the numbers
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
                    isResist = false // 👈 Explicitly marked as a failure
                )
            )
        }
    }

    fun logVictory(level: Int) {
        viewModelScope.launch {
            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = true,
                    intensity = level
                )
            )
        }
    }

    // Add this inside your HomeViewModel class
    fun logEvent(isResist: Boolean, intensity: Int = 0) {
        viewModelScope.launch {
            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = isResist,
                    intensity = intensity
                )
            )

            if (isResist) {
                val msg = when(intensity) {
                    1 -> "🌱 Spark extinguished! Good catch."
                    2 -> "⚔️ Stayed strong through the urge!"
                    3 -> "🏆 MASSIVE VICTORY! You conquered the pit."
                    else -> "Victory logged!"
                }
                // Use the context passed into the ViewModel
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
