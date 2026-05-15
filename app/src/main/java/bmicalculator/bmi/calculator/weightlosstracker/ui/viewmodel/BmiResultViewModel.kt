package bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.dao.BmiDao
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import android.content.Context
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BmiResultViewModel(private val bmiDao: BmiDao) : ViewModel() {

    data class BmiResultState(
        val bmi: Float = 0f,
        val gender: Int = 0,
        val age: Int = 25,
        val heightM: Float = 0f,
        val weightVal: Float = 0f,
        val weightUnit: String = "kg",
        val hVal1: Float = 0f,
        val hVal2: Int = 0,
        val hUnit: String = "cm",
        val date: String = "",
        val time: String = "",
        val isHistoryMode: Boolean = false,
        val isStandaloneMode: Boolean = false,
        val hasDatabaseRecords: Boolean = false,
        val recordId: Long = -1L,
        val sections: List<BmiConfigManager.BmiSection> = emptyList(),
        val currentSection: BmiConfigManager.BmiSection? = null,
        val minIdealWeight: Float = 0f,
        val maxIdealWeight: Float = 0f
    )

    private val _uiState = MutableLiveData<BmiResultState>()
    val uiState: LiveData<BmiResultState> get() = _uiState

    fun loadData(arguments: Bundle?) {
        viewModelScope.launch {
            val hasRecords = bmiDao.getLatestRecord() != null
            if (arguments != null && arguments.containsKey(BmiResultActivity.EXTRA_BMI)) {
                val isHistory = arguments.getBoolean(BmiResultActivity.EXTRA_HISTORY_BMI, false)
                val rawBmi = arguments.getFloat(BmiResultActivity.EXTRA_BMI)
                val bmi = (rawBmi * 10f).roundToInt() / 10f
                val gender = arguments.getInt(BmiResultActivity.EXTRA_GENDER)
                val age = arguments.getInt(BmiResultActivity.EXTRA_AGE)
                val heightM = arguments.getFloat(BmiResultActivity.EXTRA_HEIGHT_M)
                val weightVal = arguments.getFloat(BmiResultActivity.EXTRA_WEIGHT_VAL)
                val weightUnit = arguments.getString(BmiResultActivity.EXTRA_WEIGHT_UNIT) ?: "kg"
                val hVal1 = arguments.getFloat(BmiResultActivity.EXTRA_HEIGHT_VAL1)
                val hVal2 = arguments.getInt(BmiResultActivity.EXTRA_HEIGHT_VAL2)
                val hUnit = arguments.getString(BmiResultActivity.EXTRA_HEIGHT_UNIT) ?: "cm"
                val date = arguments.getString(BmiResultActivity.EXTRA_DATE) ?: ""
                val time = arguments.getString(BmiResultActivity.EXTRA_TIME) ?: ""
                val recordId = arguments.getLong(BmiResultActivity.EXTRA_RECORD_ID, -1L)

                val (sections, _) = BmiConfigManager.getConfiguration(gender, age)
                val currentSection = sections.find { bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE) }
                    ?: sections.lastOrNull()

                val normalSection = sections.find { it.categoryResId == R.string.bmi_normal }
                val minIdealBmi = normalSection?.minRange ?: 18.5f
                val maxIdealBmi = normalSection?.maxRange ?: 25.0f
                var minW = minIdealBmi * heightM * heightM
                var maxW = maxIdealBmi * heightM * heightM
                if (weightUnit == "lb") {
                    minW /= 0.45359237f
                    maxW /= 0.45359237f
                }

                _uiState.value = BmiResultState(
                    bmi = bmi,
                    gender = gender,
                    age = age,
                    heightM = heightM,
                    weightVal = weightVal,
                    weightUnit = weightUnit,
                    hVal1 = hVal1,
                    hVal2 = hVal2,
                    hUnit = hUnit,
                    date = date,
                    time = time,
                    isHistoryMode = isHistory,
                    isStandaloneMode = true,
                    hasDatabaseRecords = hasRecords,
                    recordId = recordId,
                    sections = sections,
                    currentSection = currentSection,
                    minIdealWeight = minW,
                    maxIdealWeight = maxW
                )
            } else {
                // Tab mode - load latest
                val record = bmiDao.getLatestRecord()
                if (record != null) {
                    val heightM = if (record.heightUnit == "cm") {
                        (record.heightCm ?: 0f) / 100f
                    } else {
                        val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
                        totalInches * 0.0254f
                    }

                    val bmi = record.bmi
                    val (sections, _) = BmiConfigManager.getConfiguration(if (record.gender == "Male") 0 else 1, record.age)
                    val currentSection = sections.find { bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE) }
                        ?: sections.lastOrNull()

                    val normalSection = sections.find { it.categoryResId == R.string.bmi_normal }
                    val minIdealBmi = normalSection?.minRange ?: 18.5f
                    val maxIdealBmi = normalSection?.maxRange ?: 25.0f
                    var minW = minIdealBmi * heightM * heightM
                    var maxW = maxIdealBmi * heightM * heightM
                    if (record.weightUnit == "lb") {
                        minW /= 0.45359237f
                        maxW /= 0.45359237f
                    }

                    _uiState.value = BmiResultState(
                        bmi = bmi,
                        gender = if (record.gender == "Male") 0 else 1,
                        age = record.age,
                        heightM = heightM,
                        weightVal = record.weight,
                        weightUnit = record.weightUnit,
                        hVal1 = if (record.heightUnit == "cm") record.heightCm ?: 0f else (record.heightFt ?: 0).toFloat(),
                        hVal2 = record.heightIn ?: 0,
                        hUnit = record.heightUnit,
                        date = record.date,
                        time = record.timeOfDay,
                        isHistoryMode = false,
                        isStandaloneMode = false,
                        hasDatabaseRecords = true,
                        recordId = record.id,
                        sections = sections,
                        currentSection = currentSection,
                        minIdealWeight = minW,
                        maxIdealWeight = maxW
                    )
                } else {
                    _uiState.value = BmiResultState(hasDatabaseRecords = false)
                }
            }
        }
    }


    fun saveRecord(record: BmiRecord, onComplete: () -> Unit) {
        viewModelScope.launch {
            bmiDao.insertRecord(record)
            onComplete()
        }
    }

    fun deleteRecord(recordId: Long, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (recordId != -1L) {
                bmiDao.deleteById(recordId)
            }
            val hasRemaining = bmiDao.getLatestRecord() != null
            onComplete(hasRemaining)
        }
    }

    class Factory(private val bmiDao: BmiDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BmiResultViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BmiResultViewModel(bmiDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
