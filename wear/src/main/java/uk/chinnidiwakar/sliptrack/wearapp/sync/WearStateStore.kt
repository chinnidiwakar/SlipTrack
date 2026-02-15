package uk.chinnidiwakar.sliptrack.wearapp.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "wear_state")

data class WearState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val shieldCharges: Int = 0
)

class WearStateStore(private val context: Context) {
    companion object {
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val LONGEST_STREAK = intPreferencesKey("longest_streak")
        private val SHIELD_CHARGES = intPreferencesKey("shield_charges")
    }

    val state: Flow<WearState> = context.dataStore.data.map { prefs ->
        WearState(
            currentStreak = prefs[CURRENT_STREAK] ?: 0,
            longestStreak = prefs[LONGEST_STREAK] ?: 0,
            shieldCharges = prefs[SHIELD_CHARGES] ?: 0
        )
    }

    suspend fun save(state: WearState) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_STREAK] = state.currentStreak
            prefs[LONGEST_STREAK] = state.longestStreak
            prefs[SHIELD_CHARGES] = state.shieldCharges
        }
    }
}
