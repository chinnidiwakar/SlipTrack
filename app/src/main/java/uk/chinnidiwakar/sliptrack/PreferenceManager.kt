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
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHIELD_CHARGES = intPreferencesKey("shield_charges")
        val SHIELD_AWARDED_MILESTONE = intPreferencesKey("shield_awarded_milestone")
    }

    val journeyName: Flow<String> = dataStore.data.map { prefs ->
        prefs[JOURNEY_NAME] ?: "last slip"
    }

    suspend fun saveJourneyName(name: String) {
        dataStore.edit { prefs ->
            prefs[JOURNEY_NAME] = name
        }
    }

    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "sky"
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    val shieldCharges: Flow<Int> = dataStore.data.map { prefs ->
        prefs[SHIELD_CHARGES] ?: 0
    }

    val highestShieldMilestoneAwarded: Flow<Int> = dataStore.data.map { prefs ->
        prefs[SHIELD_AWARDED_MILESTONE] ?: 0
    }

    suspend fun setShieldState(charges: Int, highestMilestoneAwarded: Int) {
        dataStore.edit { prefs ->
            prefs[SHIELD_CHARGES] = charges.coerceAtLeast(0)
            prefs[SHIELD_AWARDED_MILESTONE] = highestMilestoneAwarded.coerceAtLeast(0)
        }
    }
}
