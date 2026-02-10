package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val JOURNEY_NAME = stringPreferencesKey("journey_name")
    }

    // Get the name (defaults to "last slip" if empty)
    val journeyName: Flow<String> = dataStore.data.map { prefs ->
        prefs[JOURNEY_NAME] ?: "last slip"
    }

    // Save the name
    suspend fun saveJourneyName(name: String) {
        dataStore.edit { prefs ->
            prefs[JOURNEY_NAME] = name
        }
    }
}