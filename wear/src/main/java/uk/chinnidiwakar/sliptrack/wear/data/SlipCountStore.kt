package uk.chinnidiwakar.sliptrack.wear.data

import android.content.Context

object SlipCountStore {

    private const val PREF = "slip_prefs"
    private const val KEY = "today_count"

    fun save(context: Context, count: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY, count)
            .apply()
    }

    fun get(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY, 0)
    }
}
