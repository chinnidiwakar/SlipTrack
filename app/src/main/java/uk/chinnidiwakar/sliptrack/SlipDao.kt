package uk.chinnidiwakar.sliptrack

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SlipDao {

    @Query("SELECT * FROM slips WHERE isResist = 0 ORDER BY timestamp DESC")
    fun observeSlipsOnly(): Flow<List<SlipEvent>>

    @Query("SELECT * FROM slips WHERE isResist = 1 ORDER BY timestamp DESC")
    fun observeResistsOnly(): Flow<List<SlipEvent>>

    @Query("SELECT * FROM slips ORDER BY timestamp DESC")
    fun observeAllEvents(): Flow<List<SlipEvent>>

    @Insert
    suspend fun insertSlip(event: SlipEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SlipEvent>)

    @Query("SELECT * FROM slips ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSlip(): SlipEvent?

    @Query("SELECT * FROM slips WHERE isResist = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastActualSlip(): SlipEvent?

    @Query("SELECT * FROM slips ORDER BY timestamp DESC")
    suspend fun getAllSlips(): List<SlipEvent>

    @Query("SELECT * FROM slips")
    suspend fun getAllSlipsUnordered(): List<SlipEvent>

    @Query("SELECT * FROM slips ORDER BY timestamp DESC")
    fun observeAllSlips(): Flow<List<SlipEvent>>

    @Query("SELECT * FROM slips")
    fun observeAllSlipsUnordered(): Flow<List<SlipEvent>>

    @Query("DELETE FROM slips")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(events: List<SlipEvent>) {
        clearAll()
        insertAll(events)
    }
}
