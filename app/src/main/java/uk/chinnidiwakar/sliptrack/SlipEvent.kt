package uk.chinnidiwakar.sliptrack

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slips")
data class SlipEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val isResist: Boolean = false, // false = Slip, true = Victory
    val intensity: Int = 0,        // 1 = Early Spark, 2 = Heavy Urge, 3 = Near Miss
    val note: String? = null,      // Optional journal entry
    val trigger: String? = null    // Optional trigger label (stress, boredom, etc.)
)
