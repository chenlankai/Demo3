package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogTimePickerBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
import bmicalculator.bmi.calculator.weightlosstracker.util.setupMedicalInput
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = _binding!!

    private var isWeightInteracted = false
    private var isHeightInteracted = false

    private var currentWeightKg: Float = 63.5029318f // Default 140 lb
    private var currentHeightCm: Float = 170.0f
    
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDataInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        

        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            v.updatePadding(top = systemBars.top)
            insets
        }

        setupAgeRecyclerView()
        setupUnitToggles()
        setupGenderSelection()
        setupDateTime()
        setupListeners()
        updateGenderUI()
        setupInteractionDetection()
        loadLatestRecordOrDefaults()
    }

    private fun loadLatestRecordOrDefaults() {
        lifecycleScope.launch {
            val latestRecord = AppDatabase.getDatabase(requireContext()).bmiDao().getLatestRecord()
            if (latestRecord != null) {
                applyRecord(latestRecord)
            } else {
                initInputs()
            }
        }
    }

    private fun applyRecord(record: BmiRecord) {
        // Gender
        isMale = record.gender == "Male"
        updateGenderUI()

        // Age
        selectedAge = record.age
        binding.rvAge.post {
            binding.rvAge.scrollToPosition(selectedAge - 1)
        }

        // Interaction flags - set to true since we are loading saved data
        isWeightInteracted = true
        isHeightInteracted = true

        // Height constraints setup
        binding.etHeightCm.setupMedicalInput("cm", 1f, 250f, 1, false, 5)
        binding.etHeightFt.setupMedicalInput("'", 1f, 8f, 0, true, 1)
        binding.etHeightIn.setupMedicalInput("''", 0f, 11f, 0, true, 2)

        // Set Height current value first
        if (record.heightUnit == "cm") {
            currentHeightCm = record.heightCm ?: 170.0f
        } else {
            val ft = record.heightFt ?: 5
            val inch = record.heightIn ?: 7
            currentHeightCm = ((ft * 12) + inch) * 2.54f
        }

        // Apply Height UI
        if (record.heightUnit == "cm") {
            binding.toggleHeight.check(binding.btnCM.id)
            binding.etHeightCm.setText(String.format(Locale.US, "%.1f", currentHeightCm))
            binding.groupCm.visibility = View.VISIBLE
            binding.groupFtIn.visibility = View.GONE
        } else {
            binding.toggleHeight.check(binding.btnFTin.id)
            binding.etHeightFt.setText((record.heightFt ?: 5).toString())
            binding.etHeightIn.setText((record.heightIn ?: 7).toString())
            binding.groupCm.visibility = View.GONE
            binding.groupFtIn.visibility = View.VISIBLE
        }

        // Set Weight current value first
        if (record.weightUnit == "lb") {
            currentWeightKg = record.weight * 0.45359237f
        } else {
            currentWeightKg = record.weight
        }

        // Apply Weight UI
        if (record.weightUnit == "lb") {
            binding.toggleWeight.check(binding.btnLb.id)
            binding.etWeight.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
            binding.etWeight.setText(String.format(Locale.US, "%.2f", record.weight))
        } else {
            binding.toggleWeight.check(binding.btnKg.id)
            binding.etWeight.setupMedicalInput("kg", 1f, 250f, 2, false, 6)
            binding.etWeight.setText(String.format(Locale.US, "%.2f", record.weight))
        }
    }

    private fun initInputs() {
        binding.etWeight.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
        binding.etWeight.setText("140.00")
        currentWeightKg = 140f * 0.45359237f

        binding.etHeightCm.setupMedicalInput("cm", 1f, 250f, 1, false, 5)
        binding.etHeightCm.setText("170.0")
        currentHeightCm = 170.0f

        binding.etHeightFt.setupMedicalInput("'", 1f, 8f, 0, true, 1)
        binding.etHeightFt.setText("5")
        binding.etHeightIn.setupMedicalInput("''", 0f, 11f, 0, true, 2)
        binding.etHeightIn.setText("7")
    }

    private fun setupAgeRecyclerView() {
        val ages = (1..120).toList()
        val adapter = AgeAdapter(ages) { position ->
            binding.rvAge.smoothScrollToPosition(position)
        }
        binding.rvAge.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvAge.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvAge)

        binding.rvAge.post {
            val itemWidth = 65.dpToPx(requireContext())
            val padding = (binding.rvAge.width - itemWidth) / 2
            binding.rvAge.setPadding(padding, 0, padding, 0)
            binding.rvAge.clipToPadding = false
            binding.rvAge.scrollToPosition(selectedAge - 1)
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
        binding.toggleWeight.post { updateToggleUI(binding.toggleWeight, binding.btnLb.id) }
        binding.toggleHeight.post { updateToggleUI(binding.toggleHeight, binding.btnFTin.id) }

        binding.toggleWeight.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                if (!isWeightInteracted) {
                    when (checkedId) {
                        binding.btnLb.id -> {
                            binding.etWeight.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
                            binding.etWeight.setText("140.00")
                            currentWeightKg = 140f * 0.45359237f
                        }
                        binding.btnKg.id -> {
                            binding.etWeight.setupMedicalInput("kg", 1f, 250f, 2, false, 6)
                            binding.etWeight.setText("65.00")
                            currentWeightKg = 65.00f
                        }
                    }
                } else {
                    when (checkedId) {
                        binding.btnLb.id -> {
                            val lbValue = (currentWeightKg / 0.45359237f).coerceIn(2f, 551f)
                            binding.etWeight.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
                            binding.etWeight.setText(String.format(Locale.US, "%.2f", lbValue))
                        }
                        binding.btnKg.id -> {
                            val kgValue = currentWeightKg.coerceIn(1f, 250f)
                            binding.etWeight.setupMedicalInput("kg", 1f, 250f, 2, false, 6)
                            binding.etWeight.setText(String.format(Locale.US, "%.2f", kgValue))
                        }
                    }
                }
                updateToggleUI(group, checkedId)
            }
        }

        binding.toggleHeight.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleUI(group, checkedId)
                if (!isHeightInteracted) {
                    when (checkedId) {
                        binding.btnFTin.id -> {
                            binding.etHeightFt.setText("5")
                            binding.etHeightIn.setText("7")
                            currentHeightCm = ((5 * 12) + 7) * 2.54f
                            binding.groupFtIn.visibility = View.VISIBLE
                            binding.groupCm.visibility = View.GONE
                        }
                        binding.btnCM.id -> {
                            binding.etHeightCm.setText("170.0")
                            currentHeightCm = 170.0f
                            binding.groupFtIn.visibility = View.GONE
                            binding.groupCm.visibility = View.VISIBLE
                        }
                    }
                } else {
                    when (checkedId) {
                        binding.btnFTin.id -> {
                            val totalInches = currentHeightCm / 2.54f
                            val feet = (totalInches / 12).toInt().coerceIn(1, 8)
                            val inches = Math.round(totalInches % 12).toInt().coerceIn(0, 11)
                            binding.etHeightFt.setText(feet.toString())
                            binding.etHeightIn.setText(inches.toString())
                            binding.groupFtIn.visibility = View.VISIBLE
                            binding.groupCm.visibility = View.GONE
                        }
                        binding.btnCM.id -> {
                            binding.etHeightCm.setText(String.format(Locale.US, "%.1f", currentHeightCm.coerceIn(1f, 250f)))
                            binding.groupCm.visibility = View.VISIBLE
                            binding.groupFtIn.visibility = View.GONE
                        }
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
        binding.tvData.setOnClickListener { showDatePickerDialog() }
        binding.tvAfternoon.setOnClickListener { showTimePickerDialog() }

        binding.btnCalculate.setOnClickListener {
            val gender = if (isMale) 0 else 1
            val age = selectedAge

            val weightUnit = if (binding.toggleWeight.checkedButtonId == binding.btnLb.id) "lb" else "kg"
            val weightValue = if (weightUnit == "lb") currentWeightKg / 0.45359237f else currentWeightKg
            val weightKg = currentWeightKg

            var heightM = currentHeightCm / 100f
            val heightUnit = if (binding.toggleHeight.checkedButtonId == binding.btnFTin.id) "ft+in" else "cm"

            val hVal1: Float
            val hVal2: Int
            if (heightUnit == "ft+in") {
                val ftStr = binding.etHeightFt.text.toString().filter { it.isDigit() }
                val inStr = binding.etHeightIn.text.toString().filter { it.isDigit() }
                hVal1 = ftStr.toFloatOrNull() ?: 0f
                hVal2 = inStr.toIntOrNull() ?: 0
            } else {
                hVal1 = currentHeightCm
                hVal2 = 0
            }

            if (weightKg > 0 && heightM > 0) {
                val bmi = weightKg / (heightM * heightM)
                val intent = Intent(requireContext(), BmiResultActivity::class.java).apply {
                    putExtra("EXTRA_BMI", bmi)
                    putExtra("EXTRA_GENDER", gender)
                    putExtra("EXTRA_AGE", age)
                    putExtra("EXTRA_HEIGHT_M", heightM)
                    putExtra("EXTRA_DATE", binding.tvData.text.toString())
                    putExtra("EXTRA_TIME", binding.tvAfternoon.text.toString())
                    putExtra("EXTRA_WEIGHT_VAL", weightValue)
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
        val dialog = BottomSheetDialog(requireContext())
        dialog.behavior.isDraggable = false

        val dialogBinding = bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDatePickerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.setBackgroundColor(Color.WHITE)
            }
        }

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        val currentText = binding.tvData.text.toString()
        val parts = currentText.replace(",", "").split(" ")
        if (parts.size >= 3) {
            val mIdx = allMonths.indexOf(parts[0])
            if (mIdx != -1) calendar.set(Calendar.MONTH, mIdx)
            calendar.set(Calendar.DAY_OF_MONTH, parts[1].toIntOrNull() ?: 1)
            calendar.set(Calendar.YEAR, parts[2].toIntOrNull() ?: today.get(Calendar.YEAR))
        }

        val currentYear = today.get(Calendar.YEAR)
        val years = (1900..currentYear).map { it.toString() }
        dialogBinding.yearPicker.setData(years, (calendar.get(Calendar.YEAR) - 1900).coerceIn(0, years.size - 1))

        fun updatePickers(isInitial: Boolean = false) {
            val selectedYear = 1900 + dialogBinding.yearPicker.selectedPosition
            val monthsLimit = if (selectedYear == currentYear) today.get(Calendar.MONTH) + 1 else 12
            val monthsToShow = allMonths.take(monthsLimit)
            val initialMonth = if (isInitial) calendar.get(Calendar.MONTH) else dialogBinding.monthPicker.selectedPosition
            dialogBinding.monthPicker.setData(monthsToShow, initialMonth.coerceIn(0, monthsToShow.size - 1))

            val selectedMonth = dialogBinding.monthPicker.selectedPosition
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
            binding.tvData.text = "${allMonths[monthIdx]} $day, $year"
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTimePickerDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.behavior.isDraggable = false

        val dialogBinding = DialogTimePickerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.setBackgroundColor(Color.WHITE)
            }
        }

        val currentText = binding.tvAfternoon.text.toString()
        val initialPosition = timeOptions.indexOf(currentText).coerceAtLeast(0)
        dialogBinding.timePicker.setData(timeOptions, initialPosition)

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDone.setOnClickListener {
            binding.tvAfternoon.text = timeOptions[dialogBinding.timePicker.selectedPosition]
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupInteractionDetection() {
        binding.etWeight.doOnTextChanged { text, _, _, _ ->
            if (binding.etWeight.isFocused) {
                isWeightInteracted = true
                val value = text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                if (binding.toggleWeight.checkedButtonId == binding.btnLb.id) {
                    currentWeightKg = value * 0.45359237f
                } else {
                    currentWeightKg = value
                }
            }
        }
        binding.etHeightCm.doOnTextChanged { text, _, _, _ ->
            if (binding.etHeightCm.isFocused) {
                isHeightInteracted = true
                currentHeightCm = text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
            }
        }
        binding.etHeightFt.doOnTextChanged { _, _, _, _ ->
            if (binding.etHeightFt.isFocused) {
                isHeightInteracted = true
                updateHeightFromFtIn()
            }
        }
        binding.etHeightIn.doOnTextChanged { _, _, _, _ ->
            if (binding.etHeightIn.isFocused) {
                isHeightInteracted = true
                updateHeightFromFtIn()
            }
        }
    }

    private fun updateHeightFromFtIn() {
        val feet = binding.etHeightFt.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        val inches = binding.etHeightIn.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        currentHeightCm = ((feet * 12) + inches) * 2.54f
    }

    private fun updateItemsAlpha() {
        val layoutManager = binding.rvAge.layoutManager as? LinearLayoutManager ?: return
        val centerX = (binding.rvAge.width / 2).toFloat()
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(childCenterX - centerX)
            val maxDistance = 100.dpToPx(requireContext()).toFloat()
            val alpha = (1f - (distance / maxDistance)).coerceIn(0.1f, 1f)
            child.alpha = alpha
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
