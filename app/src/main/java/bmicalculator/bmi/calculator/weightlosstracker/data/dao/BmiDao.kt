package bmicalculator.bmi.calculator.weightlosstracker.data.dao

import androidx.room.*
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface BmiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BmiRecord)

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY CAST(date AS DATE) DESC, 
        CASE timeOfDay 
            WHEN 'Night' THEN 4 
            WHEN 'Evening' THEN 3 
            WHEN 'Afternoon' THEN 2 
            WHEN 'Morning' THEN 1 
            ELSE 0 
        END DESC, 
        timestamp DESC
    """)
    fun getAllRecords(): Flow<List<BmiRecord>>

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY CAST(date AS DATE) DESC, 
        CASE timeOfDay 
            WHEN 'Night' THEN 4 
            WHEN 'Evening' THEN 3 
            WHEN 'Afternoon' THEN 2 
            WHEN 'Morning' THEN 1 
            ELSE 0 
        END DESC, 
        timestamp DESC
        LIMIT 1
    """)
    fun getLatestRecordFlow(): Flow<BmiRecord?>

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY CAST(date AS DATE) DESC, 
        CASE timeOfDay 
            WHEN 'Night' THEN 4 
            WHEN 'Evening' THEN 3 
            WHEN 'Afternoon' THEN 2 
            WHEN 'Morning' THEN 1 
            ELSE 0 
        END DESC, 
        timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestRecord(): BmiRecord?

    @Delete
    suspend fun deleteRecord(record: BmiRecord)

    @Query("DELETE FROM bmi_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<BmiRecord>)
}