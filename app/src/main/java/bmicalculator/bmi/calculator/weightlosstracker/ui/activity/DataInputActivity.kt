package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogTimePickerBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
import bmicalculator.bmi.calculator.weightlosstracker.util.setupMedicalInput
import bmicalculator.bmi.calculator.weightlosstracker.util.systemBarsTopPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataInputBinding
    private var isMale: Boolean = true
    private var selectedAge: Int = 25
    private val allMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val timeOptions by lazy {
        listOf(
            getString(R.string.morning),
            getString(R.string.afternoon),
            getString(R.string.evening),
            getString(R.string.night)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.headerLayout.systemBarsTopPadding()

        setupAgeRecyclerView()
        setupUnitToggles()
        setupGenderSelection()
        setupDateTime()
        setupListeners()
        updateGenderUI()
        init()
    }

    private fun init() {
        binding.view1.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
        binding.view1.setText("141.50")

        binding.view11.setupMedicalInput("cm", 1f, 250f, 1, false, 5)
        binding.view2.setupMedicalInput("'", 1f, 8f, 0, true, 1)
        binding.view2.setText("5")
        binding.view3.setupMedicalInput("''", 0f, 11f, 0, true, 2)
        binding.view3.setText("10")
    }

    private fun setupAgeRecyclerView() {
        val ages = (1..120).toList()
        val adapter = AgeAdapter(ages) { position ->
            binding.rvAge.smoothScrollToPosition(position)
        }
        binding.rvAge.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAge.adapter = adapter
        
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvAge)

        binding.rvAge.post {
            val itemWidth = 65.dpToPx(this)
            val padding = (binding.rvAge.width - itemWidth) / 2
            binding.rvAge.setPadding(padding, 0, padding, 0)
            binding.rvAge.clipToPadding = false
            binding.rvAge.scrollToPosition(24)
            updateItemsAlpha()
        }
        
        binding.rvAge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerView = snapHelper.findSnapView(recyclerView.layoutManager)
                centerView?.let {
                    val position = recyclerView.layoutManager?.getPosition(it) ?: 0
                    if (position in ages.indices) {
                        selectedAge = ages[position]
                    }
                }
                updateItemsAlpha()
            }

        })
    }

    private fun setupUnitToggles() {
        binding.toggleUnit.post { updateToggleUI(binding.toggleUnit, binding.btnLb.id) }
        binding.toggleUnit1.post { updateToggleUI(binding.toggleUnit1, binding.btnFTin.id) }

        binding.toggleUnit.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val currentText = binding.view1.text.toString().filter { it.isDigit() || it == '.' }
                val currentValue = currentText.toFloatOrNull() ?: 0f

                when (checkedId) {
                    binding.btnLb.id -> {
                        val lbValue = (currentValue / 0.45359237f).coerceIn(2f, 551f)
                        binding.view1.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
                        binding.view1.setText(String.format(Locale.US, "%.2f", lbValue))
                    }
                    binding.btnKg.id -> {
                        val kgValue = (currentValue * 0.45359237f).coerceIn(1f, 250f)
                        binding.view1.setupMedicalInput("kg", 1f, 250f, 2, false, 6)
                        binding.view1.setText(String.format(Locale.US, "%.2f", kgValue))
                    }
                }
                updateToggleUI(group, checkedId)
            }
        }

        binding.toggleUnit1.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleUI(group, checkedId)
                when (checkedId) {
                    binding.btnFTin.id -> {
                        val cmText = binding.view11.text.toString().filter { it.isDigit() || it == '.' }
                        val cmValue = cmText.toFloatOrNull() ?: 170f
                        val totalInches = cmValue / 2.54f
                        val feet = (totalInches / 12).toInt().coerceIn(1, 8)
                        val inches = Math.round(totalInches % 12).toInt().coerceIn(0, 11)
                        binding.view2.setText(feet.toString())
                        binding.view3.setText(inches.toString())
                        binding.groupFtIn.visibility = View.VISIBLE
                        binding.groupCm.visibility = View.GONE
                    }
                    binding.btnCM.id -> {
                        val feet = binding.view2.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 5f
                        val inches = binding.view3.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 7f
                        val cmValue = ((feet * 12) + inches) * 2.54f
                        binding.view11.setText(String.format(Locale.US, "%.1f", cmValue.coerceIn(1f, 250f)))
                        binding.groupFtIn.visibility = View.GONE
                        binding.groupCm.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateToggleUI(group: MaterialButtonToggleGroup, checkedId: Int) {
        for (i in 0 until group.childCount) {
            val button = group.getChildAt(i) as? MaterialButton ?: continue
            updateButtonStyle(button, button.id == checkedId)
        }
    }

    private fun setupGenderSelection() {
        binding.layoutMale.setOnClickListener {
            isMale = true
            updateGenderUI()
        }
        binding.layoutFemale.setOnClickListener {
            isMale = false
            updateGenderUI()
        }
    }

    private fun updateGenderUI() {
        if (isMale) {
            binding.ivCheckMale.visibility = View.VISIBLE
            binding.ivCheckFemale.visibility = View.GONE
            binding.layoutMale.alpha = 1.0f
            binding.layoutFemale.alpha = 0.6f
        } else {
            binding.ivCheckMale.visibility = View.GONE
            binding.ivCheckFemale.visibility = View.VISIBLE
            binding.layoutMale.alpha = 0.6f
            binding.layoutFemale.alpha = 1.0f
        }
    }

    private fun setupDateTime() {
        val calendar = Calendar.getInstance()
        val monthStr = allMonths[calendar.get(Calendar.MONTH)]
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        binding.tvData.text = "$monthStr $day, $year"
    }

    private fun setupListeners() {
        binding.tvData.setOnClickListener {
            showDatePickerDialog()
        }
        binding.tvAfternoon.setOnClickListener {
            showTimePickerDialog()
        }
        binding.btnCalculate.setOnClickListener {
            val gender = if (isMale) 0 else 1
            val age = selectedAge
            
            // 获取体重及单位
            val weightText = binding.view1.text.toString().filter { it.isDigit() || it == '.' }
            val weightUnit = if (binding.toggleUnit.checkedButtonId == binding.btnLb.id) "lb" else "kg"
            var weightKg = weightText.toFloatOrNull() ?: 0f
            if (weightUnit == "lb") {
                weightKg *= 0.45359237f
            }

            // 获取身高及单位
            var heightM: Float
            val heightUnit = if (binding.toggleUnit1.checkedButtonId == binding.btnFTin.id) "ft+in" else "cm"
            val hVal1: String
            val hVal2: String
            if (heightUnit == "ft+in") {
                hVal1 = binding.view2.text.toString().filter { it.isDigit() }
                hVal2 = binding.view3.text.toString().filter { it.isDigit() }
                val feet = hVal1.toFloatOrNull() ?: 0f
                val inches = hVal2.toFloatOrNull() ?: 0f
                heightM = ((feet * 12) + inches) * 0.0254f
            } else {
                hVal1 = binding.view11.text.toString().filter { it.isDigit() || it == '.' }
                hVal2 = ""
                val cm = hVal1.toFloatOrNull() ?: 0f
                heightM = cm / 100f
            }

            if (weightKg > 0 && heightM > 0) {
                val bmi = weightKg / (heightM * heightM)
                
                val intent = Intent(this, BmiResultActivity::class.java).apply {
                    putExtra("EXTRA_BMI", bmi)
                    putExtra("EXTRA_GENDER", gender)
                    putExtra("EXTRA_AGE", age)
                    putExtra("EXTRA_HEIGHT_M", heightM)

                    putExtra("EXTRA_DATE", binding.tvData.text.toString())
                    putExtra("EXTRA_TIME", binding.tvAfternoon.text.toString())
                    putExtra("EXTRA_WEIGHT_VAL", weightText)
                    putExtra("EXTRA_WEIGHT_UNIT", weightUnit)
                    putExtra("EXTRA_HEIGHT_VAL1", hVal1)
                    putExtra("EXTRA_HEIGHT_VAL2", hVal2)
                    putExtra("EXTRA_HEIGHT_UNIT", heightUnit)
                }
                startActivity(intent)
            }
        }
    }

    private fun showDatePickerDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        dialog.behavior.isDraggable = false // 禁用滑动关闭，防止与选择器冲突
        dialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(Color.TRANSPARENT)

        val dialogBinding = bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDatePickerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        
        // 解析当前显示的日期
        val currentText = binding.tvData.text.toString()
        val parts = currentText.replace(",", "").split(" ")
        if (parts.size >= 3) {
            val mIdx = allMonths.indexOf(parts[0])
            if (mIdx != -1) calendar.set(Calendar.MONTH, mIdx)
            calendar.set(Calendar.DAY_OF_MONTH, parts[1].toIntOrNull() ?: 1)
            calendar.set(Calendar.YEAR, parts[2].toIntOrNull() ?: today.get(Calendar.YEAR))
        }

        // 1. 年份数据 (限制到今年)
        val currentYear = today.get(Calendar.YEAR)
        val years = (1900..currentYear).map { it.toString() }
        dialogBinding.yearPicker.setData(years, (calendar.get(Calendar.YEAR) - 1900).coerceIn(0, years.size - 1))

        // 联动逻辑：年份改变 -> 月份列表改变 -> 日期列表改变
        fun updatePickers(isInitial: Boolean = false) {
            val selectedYear = 1900 + dialogBinding.yearPicker.selectedPosition
            
            // 2. 月份数据 (如果今年，限制到当月)
            val monthsLimit = if (selectedYear == currentYear) today.get(Calendar.MONTH) + 1 else 12
            val monthsToShow = allMonths.take(monthsLimit)
            val initialMonth = if (isInitial) calendar.get(Calendar.MONTH) else dialogBinding.monthPicker.selectedPosition
            dialogBinding.monthPicker.setData(monthsToShow, initialMonth.coerceIn(0, monthsToShow.size - 1))
            
            // 3. 日期数据 (考虑大月小月、闰年，以及今天的限制)
            val selectedMonth = dialogBinding.monthPicker.selectedPosition
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1) // 避免月份切换时的溢出
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth)
            val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val daysLimit = if (selectedYear == currentYear && selectedMonth == today.get(Calendar.MONTH)) {
                today.get(Calendar.DAY_OF_MONTH)
            } else {
                maxDaysInMonth
            }
            
            val days = (1..daysLimit).map { it.toString() }
            val initialDay = if (isInitial) calendar.get(Calendar.DAY_OF_MONTH) - 1 else dialogBinding.dayPicker.selectedPosition
            dialogBinding.dayPicker.setData(days, initialDay.coerceIn(0, days.size - 1))
        }

        updatePickers(true)
        
        dialogBinding.yearPicker.onItemSelected = { updatePickers() }
        dialogBinding.monthPicker.onItemSelected = { updatePickers() }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDone.setOnClickListener {
            val year = 1900 + dialogBinding.yearPicker.selectedPosition
            val monthIdx = dialogBinding.monthPicker.selectedPosition
            val day = 1 + dialogBinding.dayPicker.selectedPosition
            
            val monthStr = allMonths[monthIdx]
            binding.tvData.text = "$monthStr $day, $year"
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showTimePickerDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        dialog.behavior.isDraggable = false
        dialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(Color.TRANSPARENT)

        val dialogBinding = DialogTimePickerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val currentText = binding.tvAfternoon.text.toString()
        val initialPosition = timeOptions.indexOf(currentText).coerceAtLeast(0)

        dialogBinding.timePicker.setData(timeOptions, initialPosition)

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDone.setOnClickListener {
            val selected = timeOptions[dialogBinding.timePicker.selectedPosition]
            binding.tvAfternoon.text = selected
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateButtonStyle(button: MaterialButton, isSelected: Boolean) {
        val shapeModel = button.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(100f)
            .build()
        button.shapeAppearanceModel = shapeModel

        if (isSelected) {
            button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            button.setTextColor(Color.BLACK)
            button.alpha = 1.0f
        } else {
            button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            button.setTextColor(Color.GRAY)
            button.alpha = 0.5f
        }
    }
    private fun updateItemsAlpha() {
        val layoutManager = binding.rvAge.layoutManager as? LinearLayoutManager ?: return
        val centerX = (binding.rvAge.width / 2).toFloat()
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(childCenterX - centerX)
            // 距离 0 处 alpha = 1.0，随着距离增大 alpha 递减，最大距离设为 itemWidth * 2（远大于后 alpha 0.1）
            val maxDistance = 100.dpToPx(this).toFloat() // 可根据需要调整，建议约两个item宽度
            val alpha = (1f - (distance / maxDistance)).coerceIn(0.1f, 1f)
            child.alpha = alpha
        }
    }
}
