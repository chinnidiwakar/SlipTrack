package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val JOURNEY_NAME = stringPreferencesKey("journey_name")
        val THEME_MODE = stringPreferencesKey("theme_mode") // Key for Amoled vs Material
        val STREAK_SHIELDS = intPreferencesKey("streak_shields")
    }

    // --- Journey Name ---
    val journeyName: Flow<String> = dataStore.data.map { prefs ->
        prefs[JOURNEY_NAME] ?: "last slip"
    }

    suspend fun saveJourneyName(name: String) {
        dataStore.edit { prefs ->
            prefs[JOURNEY_NAME] = name
        }
    }

    // --- Theme Mode ---
    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "sky" // Default to Amoled Sky
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    val streakShields: Flow<Int> = dataStore.data.map { prefs ->
        prefs[STREAK_SHIELDS] ?: 1
    }

    suspend fun consumeShield(): Boolean {
        var consumed = false
        dataStore.edit { prefs ->
            val current = prefs[STREAK_SHIELDS] ?: 1
            if (current > 0) {
                prefs[STREAK_SHIELDS] = current - 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun awardShield(count: Int = 1) {
        if (count <= 0) return
        dataStore.edit { prefs ->
            val current = prefs[STREAK_SHIELDS] ?: 1
            prefs[STREAK_SHIELDS] = current + count
        }
    }
}
