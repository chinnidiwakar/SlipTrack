package uk.chinnidiwakar.sliptrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.utils.formatElapsedTime
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp
import uk.chinnidiwakar.sliptrack.utils.toLocalDate

class HomeViewModel(
    private val dao: SlipDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _dailyQuote = MutableStateFlow("")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    private val _elapsedText = MutableStateFlow("0m")
    val elapsedText: StateFlow<String> = _elapsedText

    val journeyName: StateFlow<String> = preferenceManager.journeyName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "last slip"
        )

    // Function to update the name (called from Settings)
    fun updateJourneyName(newName: String) {
        viewModelScope.launch {
            preferenceManager.saveJourneyName(newName)
        }
    }

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
                    actualSlips.isNotEmpty() -> actualSlips.maxBy { normalizeTimestamp(it.timestamp) }.timestamp
                    allEvents.isNotEmpty() -> allEvents.minBy { normalizeTimestamp(it.timestamp) }.timestamp
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

    // 1. Add the state flow for the UI to collect
    val themeMode: StateFlow<String> = preferenceManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "sky"
        )

    // 2. Add the update function
    fun updateTheme(newMode: String) {
        viewModelScope.launch {
            preferenceManager.saveThemeMode(newMode)
        }
    }

    private fun daysSince(rawTimestamp: Long): Int {
        val date = toLocalDate(rawTimestamp)
        return java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now()).toInt()
    }

    fun logSlip(triggerLabel: String? = null) {
        viewModelScope.launch {
            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = false,
                    trigger = triggerLabel
                )
            )
            _uiMessages.emit("Slip logged. Restarting with awareness 💛")
        }
    }

    fun logEvent(
        isResist: Boolean,
        intensity: Int = 0,
        triggerLabel: String? = null
    ) {
        viewModelScope.launch {
            val safeIntensity = intensity.coerceIn(0, 3)

            dao.insertSlip(
                SlipEvent(
                    timestamp = System.currentTimeMillis(),
                    isResist = isResist,
                    intensity = safeIntensity,
                    trigger = triggerLabel
                )
            )

            if (isResist) {
                val msg = when (safeIntensity) {
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