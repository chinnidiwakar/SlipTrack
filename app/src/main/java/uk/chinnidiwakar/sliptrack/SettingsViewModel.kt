package uk.chinnidiwakar.sliptrack

import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(context: android.content.Context) : androidx.lifecycle.ViewModel() {
    private val prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)

    // Using StateFlow so the UI updates instantly when toggled
    private val _isAmoled = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("amoled", false))
    val isAmoled = _isAmoled.asStateFlow()

    private val _useDynamicColor = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("dynamic", true))
    val useDynamicColor = _useDynamicColor.asStateFlow()

    fun toggleAmoled(enabled: Boolean) {
        _isAmoled.value = enabled
        prefs.edit().putBoolean("amoled", enabled).apply()
    }

    fun toggleDynamic(enabled: Boolean) {
        _useDynamicColor.value = enabled
        prefs.edit().putBoolean("dynamic", enabled).apply()
    }
}