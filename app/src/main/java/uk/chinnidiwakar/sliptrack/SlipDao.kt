package uk.chinnidiwakar.sliptrack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow   // 👈 add this

@Dao
interface SlipDao {

    @Insert
    suspend fun insertSlip(event: SlipEvent)

    @Query("SELECT * FROM slips ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSlip(): SlipEvent?

    @Query("SELECT * FROM slips ORDER BY timestamp DESC")
    suspend fun getAllSlips(): List<SlipEvent>

    @Query("SELECT * FROM slips")
    suspend fun getAllSlipsUnordered(): List<SlipEvent>

    // ✅ NEW: Reactive versions (add these)
    @Query("SELECT * FROM slips ORDER BY timestamp DESC")
    fun observeAllSlips(): Flow<List<SlipEvent>>

    @Query("SELECT * FROM slips")
    fun observeAllSlipsUnordered(): Flow<List<SlipEvent>>
}
