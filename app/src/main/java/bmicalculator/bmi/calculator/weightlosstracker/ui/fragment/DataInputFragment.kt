package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

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
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
import bmicalculator.bmi.calculator.weightlosstracker.util.setupMedicalInput
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.*
import kotlin.math.abs

class DataInputFragment : Fragment() {

    private var _binding: ActivityDataInputBinding? = null
    private val binding get() = _binding!!
    
    private var isMale: Boolean = true
    private var selectedAge: Int = 25
    private val allMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityDataInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Hide header if it's in main screen, or customize it
        binding.headerLayout.visibility = View.GONE

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
        binding.rvAge.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvAge.adapter = adapter
        
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvAge)

        binding.rvAge.post {
            val itemWidth = 65.dpToPx(requireContext())
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
        binding.btnCalculate.setOnClickListener {
            // Logic to calculate and navigate to ResultFragment or ResultActivity
            // For now, let's keep it simple or implement the logic here
        }
    }

    private fun updateButtonStyle(button: MaterialButton, isSelected: Boolean) {
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
