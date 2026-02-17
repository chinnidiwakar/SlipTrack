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
        val STREAK_SHIELDS = intPreferencesKey("streak_shields")
        val RESIST_EVENTS_COUNT = intPreferencesKey("resist_events_count")
        val RESIST_SHIELDS_CLAIMED = intPreferencesKey("resist_shields_claimed")
        val LAST_SHIELD_MILESTONE_AWARDED = intPreferencesKey("last_shield_milestone_awarded")
        val LAST_NOTIFIED_STREAK_MILESTONE = intPreferencesKey("last_notified_streak_milestone")
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

    suspend fun rewardForResistIfEligible(): Int {
        var newShields = 0
        dataStore.edit { prefs ->
            val totalResists = (prefs[RESIST_EVENTS_COUNT] ?: 0) + 1
            prefs[RESIST_EVENTS_COUNT] = totalResists

            val claimed = prefs[RESIST_SHIELDS_CLAIMED] ?: 0
            val eligible = ShieldPolicy.shieldsFromResists(totalResists)
            val delta = (eligible - claimed).coerceAtLeast(0)

            if (delta > 0) {
                val currentShields = prefs[STREAK_SHIELDS] ?: 1
                prefs[STREAK_SHIELDS] = currentShields + delta
                prefs[RESIST_SHIELDS_CLAIMED] = claimed + delta
                newShields = delta
            }
        }
        return newShields
    }

    suspend fun rewardForMilestoneIfEligible(streakDays: Int): Int {
        var newShields = 0
        dataStore.edit { prefs ->
            val previousMilestone = prefs[LAST_SHIELD_MILESTONE_AWARDED] ?: 0
            val milestoneAwards = ShieldPolicy.countNewMilestoneShields(previousMilestone, streakDays)

            if (milestoneAwards > 0) {
                val currentShields = prefs[STREAK_SHIELDS] ?: 1
                prefs[STREAK_SHIELDS] = currentShields + milestoneAwards
                prefs[LAST_SHIELD_MILESTONE_AWARDED] = ShieldPolicy.highestReachedMilestone(streakDays)
                newShields = milestoneAwards
            }
        }
        return newShields
    }

    suspend fun shouldNotifyMilestone(streakDays: Int): Boolean {
        var shouldNotify = false
        dataStore.edit { prefs ->
            val lastNotified = prefs[LAST_NOTIFIED_STREAK_MILESTONE] ?: 0
            if (streakDays > lastNotified) {
                prefs[LAST_NOTIFIED_STREAK_MILESTONE] = streakDays
                shouldNotify = true
            }
        }
        return shouldNotify
    }
}
