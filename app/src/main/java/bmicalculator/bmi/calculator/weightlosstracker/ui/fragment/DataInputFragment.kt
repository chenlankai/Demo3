package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogTimePickerBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.DataInputViewModel
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
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

        val touchListener = View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocus = activity?.currentFocus
                if (currentFocus is EditText) {
                    val outRect = android.graphics.Rect()
                    currentFocus.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        currentFocus.clearFocus()
                        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                    }
                }
            }
            false
        }
        binding.root.setOnTouchListener(touchListener)
        binding.innerLayout.setOnTouchListener(touchListener)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!isKeyboardVisible) {
                val currentFocus = activity?.currentFocus
                if (currentFocus is EditText) {
                    currentFocus.clearFocus()
                }
            }
            insets
        }

        setupAgeRecyclerView()
        setupUnitToggles()
        setupGenderSelection()
        setupListeners()
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

        viewModel.weight.observe(viewLifecycleOwner) { kg ->
            val unit = viewModel.weightUnit.value ?: "lb"
            if (unit == "lb") {
                if (!binding.etWeight.isFocused) {
                    val lbValue = kg / 0.45359237f
                    binding.etWeight.setText(String.format(Locale.US, "%.2f", lbValue))
                }
            } else {
                if (!binding.etWeightKg.isFocused) {
                    binding.etWeightKg.setText(String.format(Locale.US, "%.2f", kg))
                }
            }
        }
        
        viewModel.height.observe(viewLifecycleOwner) { cm ->
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
                    binding.etHeightFt.setText("${feet}'")
                    binding.etHeightIn.setText("${inches}\"")
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
            val currentKg = viewModel.weight.value ?: 0f
            if (unit == "lb") {
                val lbValue = currentKg / 0.45359237f
                if (currentKg > 0) {
                    binding.etWeight.setText(String.format(Locale.US, "%.2f", lbValue))
                }
            } else {
                if (currentKg > 0) {
                    binding.etWeightKg.setText(String.format(Locale.US, "%.2f", currentKg))
                }
            }
        }
        
        viewModel.heightUnit.observe(viewLifecycleOwner) { unit ->
            val checkId = if (unit == "cm") binding.btnCM.id else binding.btnFTin.id
            if (binding.toggleHeight.checkedButtonId != checkId) {
                binding.toggleHeight.check(checkId)
            }
            updateHeightVisibility(unit)
            updateToggleUI(binding.toggleHeight, checkId)

            val currentCm = viewModel.height.value ?: 0f
            if (unit == "cm") {
                binding.etHeightCm.setText(String.format(Locale.US, "%.1f", currentCm))
            } else {
                val totalInches = currentCm / 2.54f
                val feet = (totalInches / 12).toInt().coerceIn(1, 8)
                val inches = Math.round(totalInches % 12).toInt().coerceIn(0, 11)
                binding.etHeightFt.setText("${feet}'")
                binding.etHeightIn.setText("${inches}\"")
            }
        }
    }

    private fun updateWeightInputConfig(unit: String) {
        if (unit == "lb") {
            binding.etWeight.visibility = View.VISIBLE
            binding.etWeightKg.visibility = View.GONE
            binding.etWeight.setupValidation("lb", 2f, 551f, 2, false, 6) {
                viewModel.setWeight(it * 0.45359237f)
            }
        } else {
            binding.etWeight.visibility = View.GONE
            binding.etWeightKg.visibility = View.VISIBLE
            binding.etWeightKg.setupValidation("kg", 1f, 250f, 2, false, 6) {
                viewModel.setWeight(it)
            }
        }
    }

    private fun updateHeightVisibility(unit: String) {
        if (unit == "cm") {
            binding.groupCm.visibility = View.VISIBLE
            binding.groupFtIn.visibility = View.GONE
            binding.etHeightCm.setupValidation("cm", 1f, 250f, 1, false, 5) {
                viewModel.setHeight(it)
            }
        } else {
            binding.groupCm.visibility = View.GONE
            binding.groupFtIn.visibility = View.VISIBLE
            binding.etHeightFt.setupValidation("'", 1f, 8f, 0, true, 1) {
                updateHeightFromFtIn()
            }
            binding.etHeightIn.setupValidation("\"", 0f, 11f, 0, true, 2) {
                updateHeightFromFtIn()
            }
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
        binding.toggleWeight.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                activity?.currentFocus?.clearFocus()
                val unit = if (checkedId == binding.btnLb.id) "lb" else "kg"
                viewModel.setWeightUnit(unit)
            }
        }

        binding.toggleHeight.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                activity?.currentFocus?.clearFocus()
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
        binding.layoutMale.setOnClickListener {
            activity?.currentFocus?.clearFocus()
            viewModel.setGender(true)
        }
        binding.layoutFemale.setOnClickListener {
            activity?.currentFocus?.clearFocus()
            viewModel.setGender(false)
        }
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
        binding.ivTitleIcon.setOnClickListener {
            startActivity(Intent(requireContext(), bmicalculator.bmi.calculator.weightlosstracker.ui.activity.MeActivity::class.java))
        }
        binding.tvData.setOnClickListener { showDatePickerDialog() }
        binding.tvAfternoon.setOnClickListener { showTimePickerDialog() }

        binding.btnCalculate.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            activity?.currentFocus?.let {
                it.clearFocus()
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }

            val weightKg = viewModel.weight.value ?: 0f
            val heightCm = viewModel.height.value ?: 0f

            if (weightKg <= 0) {
                val unit = viewModel.weightUnit.value ?: "lb"
                if (unit == "lb") showValidationError("lb", 2f, 551f)
                else showValidationError("kg", 1f, 250f)
                return@setOnClickListener
            }

            if (heightCm <= 0) {
                val unit = viewModel.heightUnit.value ?: "ft+in"
                if (unit == "cm") showValidationError("cm", 1f, 250f)
                else showValidationError("ft/in", 1f, 8f)
                return@setOnClickListener
            }

            val isMale = viewModel.isMale.value ?: true
            val age = viewModel.selectedAge.value ?: 25
            
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


    private fun EditText.setupValidation(
        unit: String,
        min: Float,
        max: Float,
        decimalDigits: Int,
        showUnitDuringEdit: Boolean,
        maxLen: Int,
        onValid: (Float) -> Unit
    ) {
        this.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (showUnitDuringEdit) {
                    val currentText = text.toString()
                    if (!currentText.endsWith(unit)) {
                        val numeric = currentText.filter { it.isDigit() || it == '.' }
                        setText("$numeric$unit")
                        setSelection(numeric.length)
                    } else {
                        setSelection(currentText.length - unit.length)
                    }
                } else {
                    val rawInput = text.toString().filter { it.isDigit() || it == '.' }
                    setText(rawInput)
                    if (rawInput.isNotEmpty()) {
                        setSelection(rawInput.length)
                    }
                }
            } else {
                performValidation(unit, min, max, onValid)
            }
        }

        if (showUnitDuringEdit) {
            this.setOnTouchListener { v, event ->
                val et = v as EditText
                val s = et.text.toString()
                if (s.endsWith(unit)) {
                    val numericLength = s.length - unit.length
                    val offset = et.getOffsetForPosition(event.x, event.y)
                    if (offset >= numericLength) {
                        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                            if (!et.isFocused) {
                                et.requestFocus()
                                val imm = et.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
                            }
                            et.setSelection(numericLength)
                        } else if (event.action == MotionEvent.ACTION_UP) {
                            et.performClick()
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }

            this.doOnTextChanged { text, _, _, _ ->
                if (isFocused) {
                    val s = text.toString()
                    if (!s.endsWith(unit)) {
                        val numeric = s.filter { it.isDigit() || it == '.' }
                        val newText = "$numeric$unit"
                        setText(newText)
                        setSelection(numeric.length)
                    } else {
                        val numericLength = s.length - unit.length
                        val start = selectionStart.coerceAtMost(numericLength)
                        val end = selectionEnd.coerceAtMost(numericLength)
                        if (selectionStart != start || selectionEnd != end) {
                            android.text.Selection.setSelection(getText(), start, end)
                        }
                    }
                }
            }

            this.accessibilityDelegate = object : View.AccessibilityDelegate() {
                private var isChanging = false
                override fun sendAccessibilityEvent(host: View, eventType: Int) {
                    if (eventType == android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                        val et = host as? EditText
                        val s = et?.text?.toString() ?: ""
                        if (s.endsWith(unit)) {
                            val numericLength = s.length - unit.length
                            if (et != null && (et.selectionStart > numericLength || et.selectionEnd > numericLength)) {
                                if (!isChanging) {
                                    isChanging = true
                                    et.setSelection(
                                        et.selectionStart.coerceAtMost(numericLength),
                                        et.selectionEnd.coerceAtMost(numericLength)
                                    )
                                    isChanging = false
                                }
                            }
                        }
                    }
                    super.sendAccessibilityEvent(host, eventType)
                }
            }
        }

        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performValidation(unit, min, max, onValid)
                this.clearFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun EditText.performValidation(
        unit: String,
        min: Float,
        max: Float,
        onValid: (Float) -> Unit
    ) {
        val rawInput = text.toString().filter { it.isDigit() || it == '.' }
        val input = rawInput.toFloatOrNull()
        
        var resultValue: Float
        var shouldShowError: Boolean

        if (input == null) {
            if (this.id == binding.etHeightIn.id) {
                resultValue = 0f
                shouldShowError = false
            } else {
                resultValue = when (this.id) {
                    binding.etWeight.id -> 140f
                    binding.etWeightKg.id -> 60f
                    binding.etHeightCm.id -> 170f
                    binding.etHeightFt.id -> 5f
                    else -> min
                }
                shouldShowError = true
            }
        } else if (input < min) {
            resultValue = min
            shouldShowError = true
        } else if (input > max) {
            resultValue = max
            shouldShowError = true
        } else {
            resultValue = input
            shouldShowError = false
        }

        if (shouldShowError) {
            showValidationError(unit, min, max)
        }

        val formattedValue = when (this.id) {
            binding.etWeight.id, binding.etWeightKg.id -> String.format(Locale.US, "%.2f", resultValue)
            binding.etHeightCm.id -> String.format(Locale.US, "%.1f", resultValue)
            else -> "${resultValue.toInt()}$unit"
        }
        setText(formattedValue)
        onValid(resultValue)
    }

    private fun showValidationError(unit: String, min: Float, max: Float) {

        val minStr = if (min == min.toInt().toFloat()) min.toInt().toString() else String.format(Locale.US, "%.1f", min)
        val maxStr = if (max == max.toInt().toFloat()) max.toInt().toString() else String.format(Locale.US, "%.1f", max)

        val message = if (unit == "kg" || unit == "lb") {
            "Please input a valid weight ($minStr - $maxStr $unit) to calculate your BMI accurately"
        } else {
            val displayUnit = if (unit == "'" || unit == "\"" || unit == "ft/in") "ft/in" else unit
            "Please input a valid Height ($minStr - $maxStr $displayUnit) to calculate your BMI accurately"
        }
        (requireActivity() as? BaseActivity)?.showStatusToast(
            message,
            R.drawable.ic_report_problem,
            "#2196F3"
        )

    }

    private fun updateHeightFromFtIn() {
        val feet = binding.etHeightFt.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        val inches = binding.etHeightIn.text.toString().filter { it.isDigit() }.toFloatOrNull() ?: 0f
        viewModel.setHeight(((feet * 12) + inches) * 2.54f)
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
