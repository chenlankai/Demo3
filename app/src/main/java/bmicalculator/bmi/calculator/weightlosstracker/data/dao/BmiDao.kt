package bmicalculator.bmi.calculator.weightlosstracker.data.dao

import androidx.room.*
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BmiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BmiRecord)

    @Query("SELECT * FROM bmi_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<BmiRecord>>

    @Query("SELECT * FROM bmi_records ORDER BY id DESC LIMIT 1")
    fun getLatestRecordFlow(): Flow<BmiRecord?>

    @Query("SELECT * FROM bmi_records ORDER BY id DESC LIMIT 1")
    suspend fun getLatestRecord(): BmiRecord?

    @Delete
    suspend fun deleteRecord(record: BmiRecord)

    @Query("DELETE FROM bmi_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
