package bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import bmicalculator.bmi.calculator.weightlosstracker.data.dao.BmiDao
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val bmiDao: BmiDao) : ViewModel() {
    private val _selectedTab = MutableLiveData<Int>()
    val selectedTab: LiveData<Int> get() = _selectedTab

    private val _bmiValue = MutableLiveData<Float>()
    val bmiValue: LiveData<Float> get() = _bmiValue

    private val _bmiCategory = MutableLiveData<Int>()
    val bmiCategory: LiveData<Int> get() = _bmiCategory

    val latestRecord: LiveData<BmiRecord?> = bmiDao.getLatestRecordFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        .asLiveData()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun updateBmiData(bmi: Float, categoryResId: Int) {
        _bmiValue.value = bmi
        _bmiCategory.value = categoryResId
    }

    class Factory(private val bmiDao: BmiDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(bmiDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
