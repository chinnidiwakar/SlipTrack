package uk.chinnidiwakar.sliptrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slips")
data class SlipEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long
)
