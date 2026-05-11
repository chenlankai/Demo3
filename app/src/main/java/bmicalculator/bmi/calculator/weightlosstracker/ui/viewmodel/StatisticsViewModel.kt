package bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel

import androidx.lifecycle.*
import bmicalculator.bmi.calculator.weightlosstracker.data.dao.BmiDao
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord

class StatisticsViewModel(private val bmiDao: BmiDao) : ViewModel() {

    val allRecords: LiveData<List<BmiRecord>> = bmiDao.getAllRecords().asLiveData()

    class Factory(private val bmiDao: BmiDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(bmiDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}