package bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import bmicalculator.bmi.calculator.weightlosstracker.data.dao.BmiDao
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import kotlinx.coroutines.launch
import java.util.*

class DataInputViewModel(private val bmiDao: BmiDao) : ViewModel() {

    private val _isMale = MutableLiveData(true)
    val isMale: LiveData<Boolean> get() = _isMale

    private val _selectedAge = MutableLiveData(25)
    val selectedAge: LiveData<Int> get() = _selectedAge

    private val _weight = MutableLiveData(140f) // Default 140 lb
    val weight: LiveData<Float> get() = _weight

    private val _height = MutableLiveData(170.18f) // Default 5ft 7 in
    val height: LiveData<Float> get() = _height

    private val _weightUnit = MutableLiveData("lb")
    val weightUnit: LiveData<String> get() = _weightUnit

    private val _heightUnit = MutableLiveData("ft+in")
    val heightUnit: LiveData<String> get() = _heightUnit

    private val _isWeightInteracted = MutableLiveData(false)
    val isWeightInteracted: LiveData<Boolean> get() = _isWeightInteracted

    private val _isHeightInteracted = MutableLiveData(false)
    val isHeightInteracted: LiveData<Boolean> get() = _isHeightInteracted

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = _selectedDate

    private val _selectedTime = MutableLiveData<String>()
    val selectedTime: LiveData<String> get() = _selectedTime

    private val _latestRecord = MutableLiveData<BmiRecord?>()
    val latestRecord: LiveData<BmiRecord?> get() = _latestRecord

    init {
        val calendar = Calendar.getInstance()

        // 1. 设置日期
        val monthStr = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")[calendar.get(Calendar.MONTH)]
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        _selectedDate.value = "$monthStr $day, $year"
        _weightUnit.value = "lb"
        _weight.value = 140f


        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // 获取当前 24 小时制的小时

        _selectedTime.value = when (currentHour) {
            in 8..13 -> "Morning"
            in 14..18 -> "Afternoon"
            in 19..22 -> "Evening"
            else -> "Night"
        }
    }

    fun setGender(isMale: Boolean) {
        _isMale.value = isMale
    }

    fun setAge(age: Int) {
        _selectedAge.value = age
    }

    fun setWeight(weightValue: Float) {
        _weight.value = weightValue
        _isWeightInteracted.value = true
    }

    fun setHeight(heightValue: Float) {
        _height.value = heightValue
        _isHeightInteracted.value = true
    }

    fun setWeightUnit(unit: String) {
        if (_isWeightInteracted.value != true) {
            if (unit == "lb") {
                _weight.value = 140f
            } else {
                _weight.value = 65.0f
            }
        }
        _weightUnit.value = unit
    }

    fun setHeightUnit(unit: String) {
        if (_isHeightInteracted.value != true) {
            if (unit == "cm") {
                _height.value = 170.0f // 默认 170.0 cm
            } else {
                _height.value = 170.18f // 对应 5'7" (67 inches * 2.54)
            }
        }
        _heightUnit.value = unit
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun setTime(time: String) {
        _selectedTime.value = time
    }

    fun saveDraft(context: Context) {
        val prefs = context.getSharedPreferences("bmi_input_draft", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_male", _isMale.value ?: true)
            putInt("age", _selectedAge.value ?: 25)
            putFloat("weight", _weight.value ?: 140f)
            putFloat("height", _height.value ?: 170.18f)
            putString("weight_unit", _weightUnit.value ?: "lb")
            putString("height_unit", _heightUnit.value ?: "ft+in")
            putBoolean("weight_interacted", _isWeightInteracted.value ?: false)
            putBoolean("height_interacted", _isHeightInteracted.value ?: false)
            apply()
        }
    }

    fun loadLatestRecord(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("bmi_input_draft", Context.MODE_PRIVATE)

            // 优先级 1: 检查是否有上次修改的草稿
            if (prefs.contains("is_male")) {
                _isMale.value = prefs.getBoolean("is_male", true)
                _selectedAge.value = prefs.getInt("age", 25)
                _weight.value = prefs.getFloat("weight", 140f)
                _height.value = prefs.getFloat("height", 170.18f)
                _weightUnit.value = prefs.getString("weight_unit", "lb")
                _heightUnit.value = prefs.getString("height_unit", "ft+in")
                _isWeightInteracted.value = prefs.getBoolean("weight_interacted", false)
                _isHeightInteracted.value = prefs.getBoolean("height_interacted", false)

                // 同时也要获取最新记录供其他逻辑使用，但不覆盖当前 UI 状态
                _latestRecord.value = bmiDao.getLatestRecord()
            } else {
                // 优先级 2: 如果没有草稿，加载数据库最新记录
                val record = bmiDao.getLatestRecord()
                _latestRecord.value = record
                record?.let {
                    _isMale.value = it.gender == "Male"
                    _selectedAge.value = it.age
                    _weightUnit.value = it.weightUnit
                    _heightUnit.value = it.heightUnit
                    _weight.value = it.weight

                    if (it.heightUnit == "cm") {
                        _height.value = it.heightCm ?: 170.18f
                    } else {
                        val ft = it.heightFt ?: 5
                        val inch = it.heightIn ?: 7
                        _height.value = ((ft * 12) + inch) * 2.54f
                    }
                    
                    _isWeightInteracted.value = true
                    _isHeightInteracted.value = true
                }
                // 优先级 3: 如果连数据库记录都没有，则保留 ViewModel 初始化的默认值
            }
        }
    }

    class Factory(private val bmiDao: BmiDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DataInputViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DataInputViewModel(bmiDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
