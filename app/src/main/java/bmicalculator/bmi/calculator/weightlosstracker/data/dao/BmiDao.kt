package bmicalculator.bmi.calculator.weightlosstracker.data.dao

import androidx.room.*
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BmiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BmiRecord)

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY 
            (
                SUBSTR(date, 9, 4) ||  
                CASE SUBSTR(date, 1, 3) 
                    WHEN 'Jan' THEN '01' WHEN 'Feb' THEN '02' WHEN 'Mar' THEN '03' 
                    WHEN 'Apr' THEN '04' WHEN 'May' THEN '05' WHEN 'Jun' THEN '06' 
                    WHEN 'Jul' THEN '07' WHEN 'Aug' THEN '08' WHEN 'Sep' THEN '09' 
                    WHEN 'Oct' THEN '10' WHEN 'Nov' THEN '11' WHEN 'Dec' THEN '12' 
                    ELSE '00' 
                END ||
                SUBSTR(date, 5, 2)     
            ) DESC,
            CASE timeOfDay 
                WHEN 'Night'     THEN 4 
                WHEN 'Evening'   THEN 3 
                WHEN 'Afternoon' THEN 2 
                WHEN 'Morning'   THEN 1 
                ELSE 0 
            END DESC,
            timestamp DESC
    """)
    fun getAllRecords(): Flow<List<BmiRecord>>

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY 
            (SUBSTR(date, 9, 4) || 
             CASE SUBSTR(date, 1, 3)
                WHEN 'Jan' THEN '01' WHEN 'Feb' THEN '02' WHEN 'Mar' THEN '03' 
                WHEN 'Apr' THEN '04' WHEN 'May' THEN '05' WHEN 'Jun' THEN '06' 
                WHEN 'Jul' THEN '07' WHEN 'Aug' THEN '08' WHEN 'Sep' THEN '09' 
                WHEN 'Oct' THEN '10' WHEN 'Nov' THEN '11' WHEN 'Dec' THEN '12' 
                ELSE '00' 
             END ||
             SUBSTR(date, 5, 2)
            ) DESC,
            CASE timeOfDay 
                WHEN 'Night'     THEN 4 
                WHEN 'Evening'   THEN 3 
                WHEN 'Afternoon' THEN 2 
                WHEN 'Morning'   THEN 1 
                ELSE 0 
            END DESC,
            timestamp DESC
        LIMIT 1
    """)
    fun getLatestRecordFlow(): Flow<BmiRecord?>

    @Query("""
        SELECT * FROM bmi_records 
        ORDER BY 
            (SUBSTR(date, 9, 4) || 
             CASE SUBSTR(date, 1, 3)
                WHEN 'Jan' THEN '01' WHEN 'Feb' THEN '02' WHEN 'Mar' THEN '03' 
                WHEN 'Apr' THEN '04' WHEN 'May' THEN '05' WHEN 'Jun' THEN '06' 
                WHEN 'Jul' THEN '07' WHEN 'Aug' THEN '08' WHEN 'Sep' THEN '09' 
                WHEN 'Oct' THEN '10' WHEN 'Nov' THEN '11' WHEN 'Dec' THEN '12' 
                ELSE '00' 
             END ||
             SUBSTR(date, 5, 2)
            ) DESC,
            CASE timeOfDay 
                WHEN 'Night'     THEN 4 
                WHEN 'Evening'   THEN 3 
                WHEN 'Afternoon' THEN 2 
                WHEN 'Morning'   THEN 1 
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