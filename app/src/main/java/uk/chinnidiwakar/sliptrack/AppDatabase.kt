package uk.chinnidiwakar.sliptrack

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SlipEvent::class], version = 2) // 1. Bump version to 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun slipDao(): SlipDao
}

