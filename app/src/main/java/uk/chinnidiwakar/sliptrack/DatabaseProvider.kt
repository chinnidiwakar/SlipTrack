package uk.chinnidiwakar.sliptrack

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var db: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sliptrack.db"
            )
                .fallbackToDestructiveMigration() // 2. Add this to auto-clear old data
                .build()

            db = instance
            instance
        }
    }
}