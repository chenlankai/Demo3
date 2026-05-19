package bmicalculator.bmi.calculator.weightlosstracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bmi_records")
data class BmiRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,                // 体重数值
    val weightUnit: String,          // "kg" 或 "lb"
    val heightCm: Float? = null,     // 当身高单位为 cm 时使用
    val heightFt: Int? = null,       // 身高单位为 ft-in 时的英尺
    val heightIn: Int? = null,       // 身高单位为 ft-in 时的英寸
    val heightUnit: String,          // "cm" 或 "ft+in"
    val date: String,                // 格式如 "YYYY-MM-DD"
    val timeOfDay: String,           // "Morning", "Afternoon", "Evening" "Night"
    val age: Int,
    val gender: String,              // "Male" 或 "Female"
    val bmi: Float,                  // BMI 数值
    val timestamp: Long = System.currentTimeMillis() // 真实创建时间
)