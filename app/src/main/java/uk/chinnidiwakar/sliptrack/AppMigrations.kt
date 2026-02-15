package uk.chinnidiwakar.sliptrack

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE slips ADD COLUMN trigger TEXT")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_slips_timestamp ON slips(timestamp)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_slips_isResist_timestamp ON slips(isResist, timestamp)")
        }
    }
}
