package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogTimePickerBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.DataInputViewModel
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
import bmicalculator.bmi.calculator.weightlosstracker.util.setupMedicalInput
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.*
import kotlin.math.abs

class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DataInputViewModel by viewModels {
        DataInputViewModel.Factory(AppDatabase.getDatabase(requireContext()).bmiDao())
    }

    private val allMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val timeKeys = listOf("Morning", "Afternoon", "Evening", "Night")
    private val timeLabels by lazy {
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
        setupListeners()
        setupInteractionDetection()
        
        observeViewModel()
        
        viewModel.loadLatestRecord()
    }

    private fun observeViewModel() {
        viewModel.isMale.observe(viewLifecycleOwner) { isMale ->
            updateGenderUI(isMale)
        }

        viewModel.selectedAge.observe(viewLifecycleOwner) { age ->
            binding.rvAge.post {
                val layoutManager = binding.rvAge.layoutManager as? LinearLayoutManager
                val first = layoutManager?.findFirstVisibleItemPosition() ?: -1
                val last = layoutManager?.findLastVisibleItemPosition() ?: -1
                if (age - 1 !in first..last) {
                    binding.rvAge.scrollToPosition(age - 1)
                }
            }
        }

        viewModel.weightKg.observe(viewLifecycleOwner) { kg ->
            if (!binding.etWeight.isFocused) {
                val unit = viewModel.weightUnit.value ?: "lb"
                if (unit == "lb") {
                    val lbValue = kg / 0.45359237f
                    binding.etWeight.setText(String.format(Locale.US, "%.2f", lbValue))
                } else {
                    binding.etWeight.setText(String.format(Locale.US, "%.2f", kg))
                }
            }
        }
        
        viewModel.heightCm.observe(viewLifecycleOwner) { cm ->
            val unit = viewModel.heightUnit.value ?: "ft+in"
            if (unit == "cm") {
                if (!binding.etHeightCm.isFocused) {
                    binding.etHeightCm.setText(String.format(Locale.US, "%.1f", cm))
                }
            } else {
                if (!binding.etHeightFt.isFocused && !binding.etHeightIn.isFocused) {
                    val totalInches = cm / 2.54f
                    val feet = (totalInches / 12).toInt().coerceIn(1, 8)
                    val inches = Math.round(totalInches % 12).toInt().coerceIn(0, 11)
                    binding.etHeightFt.setText(feet.toString())
                    binding.etHeightIn.setText(inches.toString())
                }
            }
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.tvData.text = date
        }

        viewModel.selectedTime.observe(viewLifecycleOwner) { key ->
            val index = timeKeys.indexOf(key).coerceAtLeast(0)
            binding.tvAfternoon.text = timeLabels[index]
        }
        
        viewModel.weightUnit.observe(viewLifecycleOwner) { unit ->
            val checkId = if (unit == "lb") binding.btnLb.id else binding.btnKg.id
            if (binding.toggleWeight.checkedButtonId != checkId) {
                binding.toggleWeight.check(checkId)
            }
            updateWeightInputConfig(unit)
            updateToggleUI(binding.toggleWeight, checkId)
        }
        
        viewModel.heightUnit.observe(viewLifecycleOwner) { unit ->
            val checkId = if (unit == "cm") binding.btnCM.id else binding.btnFTin.id
            if (binding.toggleHeight.checkedButtonId != checkId) {
                binding.toggleHeight.check(checkId)
            }
            updateHeightVisibility(unit)
            updateToggleUI(binding.toggleHeight, checkId)
        }
    }

    private fun updateWeightInputConfig(unit: String) {
        if (unit == "lb") {
            binding.etWeight.setupMedicalInput("lb", 2f, 551f, 2, false, 6)
        } else {
            binding.etWeight.setupMedicalInput("kg", 1f, 250f, 2, false, 6)
        }
    }

    private fun updateHeightVisibility(unit: String) {
        if (unit == "cm") {
            binding.groupCm.visibility = View.VISIBLE
            binding.groupFtIn.visibility = View.GONE
            binding.etHeightCm.setupMedicalInput("cm", 1f, 250f, 1, false, 5)
        } else {
            binding.groupCm.visibility = View.GONE
            binding.groupFtIn.visibility = View.VISIBLE
            binding.etHeightFt.setupMedicalInput("'", 1f, 8f, 0, true, 1)
            binding.etHeightIn.setupMedicalInput("''", 0f, 11f, 0, true, 2)
        }
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
            val currentAge = viewModel.selectedAge.value ?: 25
            binding.rvAge.scrollToPosition(currentAge - 1)
            updateItemsAlpha()
        }

        binding.rvAge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerView = snapHelper.findSnapView(recyclerView.layoutManager)
                centerView?.let {
                    val position = recyclerView.layoutManager?.getPosition(it) ?: 0
                    if (position in ages.indices) {
                        viewModel.setAge(ages[position])
                    }
                }
                updateItemsAlpha()
            }
        })
    }

    private fun setupUnitToggles() {
        binding.toggleWeight.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val unit = if (checkedId == binding.btnLb.id) "lb" else "kg"
                viewModel.setWeightUnit(unit)
            }
        }

        binding.toggleHeight.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val unit = if (checkedId == binding.btnCM.id) "cm" else "ft+in"
                viewModel.setHeightUnit(unit)
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
        binding.layoutMale.setOnClickListener { viewModel.setGender(true) }
        binding.layoutFemale.setOnClickListener { viewModel.setGender(false) }
    }

    private fun updateGenderUI(isMale: Boolean) {
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

    private fun setupListeners() {
        binding.tvData.setOnClickListener { showDatePickerDialog() }
        binding.tvAfternoon.setOnClickListener { showTimePickerDialog() }

        binding.btnCalculate.setOnClickListener {
            val isMale = viewModel.isMale.value ?: true
            val age = viewModel.selectedAge.value ?: 25
            val weightKg = viewModel.weightKg.value ?: 0f
            val heightCm = viewModel.heightCm.value ?: 0f
            
            val weightUnit = viewModel.weightUnit.value ?: "lb"
            val weightValue = if (weightUnit == "lb") weightKg / 0.45359237f else weightKg
            
            val heightUnit = viewModel.heightUnit.value ?: "ft+in"
            val heightM = heightCm / 100f

            val hVal1: Float
            val hVal2: Int
            if (heightUnit == "ft+in") {
                val totalInches = heightCm / 2.54f
                hVal1 = (totalInches / 12).toInt().toFloat()
                hVal2 = Math.round(totalInches % 12).toInt()
            } else {
                hVal1 = heightCm
                hVal2 = 0
            }

            if (weightKg > 0 && heightM > 0) {
                val bmi = weightKg / (heightM * heightM)
                val intent = Intent(requireContext(), BmiResultActivity::class.java).apply {
                    putExtra("EXTRA_BMI", bmi)
                    putExtra("EXTRA_GENDER", if (isMale) 0 else 1)
                    putExtra("EXTRA_AGE", age)
                    putExtra("EXTRA_HEIGHT_M", heightM)
                    putExtra("EXTRA_DATE", viewModel.selectedDate.value)
                    putExtra("EXTRA_TIME", viewModel.selectedTime.value)
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
            bottomSheet?.let { it.setBackgroundColor(Color.WHITE) }
        }

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        val currentText = viewModel.selectedDate.value ?: ""
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
            viewModel.setDate("${allMonths[monthIdx]} $day, $year")
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
            bottomSheet?.let { it.setBackgroundColor(Color.WHITE) }
        }

        val currentKey = viewModel.selectedTime.value ?: "Morning"
        val initialPosition = timeKeys.indexOf(currentKey).coerceAtLeast(0)
        dialogBinding.timePicker.setData(timeLabels, initialPosition)

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDone.setOnClickListener {
            val selectedKey = timeKeys[dialogBinding.timePicker.selectedPosition]
            viewModel.setTime(selectedKey)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupInteractionDetection() {
        binding.etWeight.doOnTextChanged { text, _, _, _ ->
            if (binding.etWeight.isFocused) {
                val value = text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                val unit = viewModel.weightUnit.value ?: "lb"
                if (unit == "lb") {
                    viewModel.setWeightKg(value * 0.45359237f)
                } else {
                    viewModel.setWeightKg(value)
                }
            }
        }
        binding.etHeightCm.doOnTextChanged { text, _, _, _ ->
            if (binding.etHeightCm.isFocused) {
                val cm = text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                viewModel.setHeightCm(cm)
            }
        }
        binding.etHeightFt.doOnTextChanged { _, _, _, _ ->
            if (binding.etHeightFt.isFocused) {
                updateHeightFromFtIn()
            }
        }
        binding.etHeightIn.doOnTextChanged { _, _, _, _ ->
            if (binding.etHeightIn.isFocused) {
                updateHeightFromFtIn()
            }
        }
    }

    private fun updateHeightFromFtIn() {
        val feet = binding.etHeightFt.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        val inches = binding.etHeightIn.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        viewModel.setHeightCm(((feet * 12) + inches) * 2.54f)
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
