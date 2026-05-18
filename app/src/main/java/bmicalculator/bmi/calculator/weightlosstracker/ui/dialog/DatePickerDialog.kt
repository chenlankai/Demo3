package bmicalculator.bmi.calculator.weightlosstracker.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseBottomSheetDialog
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDatePickerBinding
import java.util.Calendar

class DatePickerDialog(
    context: Context,
    private val initialDateText: String,
    private val allMonths: List<String>,
    private val onDateSelected: (String) -> Unit
) : BaseBottomSheetDialog<DialogDatePickerBinding>(context) {

    // ✨ 1. 只需要把自己的 Binding 给到基类即可
    override fun inflateBinding(inflater: LayoutInflater): DialogDatePickerBinding {
        return DialogDatePickerBinding.inflate(inflater)
    }

    // 初始化
    override fun initDialog() {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        val parts = initialDateText.replace(",", "").split(" ")
        if (parts.size >= 3) {
            val mIdx = allMonths.indexOf(parts[0])
            if (mIdx != -1) calendar.set(Calendar.MONTH, mIdx)
            calendar.set(Calendar.DAY_OF_MONTH, parts[1].toIntOrNull() ?: 1)
            calendar.set(Calendar.YEAR, parts[2].toIntOrNull() ?: today.get(Calendar.YEAR))
        }

        val currentYear = today.get(Calendar.YEAR)
        val years = (1900..currentYear).map { it.toString() }
        binding.yearPicker.setData(years, (calendar.get(Calendar.YEAR) - 1900).coerceIn(0, years.size - 1))

        fun updatePickers(isInitial: Boolean = false) {
            val selectedYear = 1900 + binding.yearPicker.selectedPosition
            val monthsLimit = if (selectedYear == currentYear) today.get(Calendar.MONTH) + 1 else 12
            val monthsToShow = allMonths.take(monthsLimit)
            val initialMonth = if (isInitial) calendar.get(Calendar.MONTH) else binding.monthPicker.selectedPosition
            binding.monthPicker.setData(monthsToShow, initialMonth.coerceIn(0, monthsToShow.size - 1))

            val selectedMonth = binding.monthPicker.selectedPosition
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth)
            val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val daysLimit = if (selectedYear == currentYear && selectedMonth == today.get(Calendar.MONTH)) {
                today.get(Calendar.DAY_OF_MONTH)
            } else {
                maxDaysInMonth
            }

            val days = (1..daysLimit).map { it.toString() }
            val initialDay = if (isInitial) calendar.get(Calendar.DAY_OF_MONTH) - 1 else binding.dayPicker.selectedPosition
            binding.dayPicker.setData(days, initialDay.coerceIn(0, days.size - 1))
        }

        updatePickers(true)
        binding.yearPicker.onItemSelected = { updatePickers() }
        binding.monthPicker.onItemSelected = { updatePickers() }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener {
            val year = 1900 + binding.yearPicker.selectedPosition
            val monthIdx = binding.monthPicker.selectedPosition
            val day = 1 + binding.dayPicker.selectedPosition

            onDateSelected("${allMonths[monthIdx]} $day, $year")
            dismiss()
        }
    }
}