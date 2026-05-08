package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityBmiResultBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class BmiResultFragment : Fragment() {

    private var _binding: ActivityBmiResultBinding? = null
    private val binding get() = _binding!!
    private val rangeAdapter = BmiRangeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityBmiResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        
        binding.tvSave.visibility = View.GONE
        binding.tvDiscard.visibility = View.GONE
        binding.layoutToolbar.visibility = View.GONE

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        binding.rvStatus.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rangeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val records = AppDatabase.getDatabase(requireContext()).bmiDao().getAllRecords().first()
            if (records.isNotEmpty()) {
                val latest = records.first()
                displayRecord(latest)
            }
        }
    }

    private fun displayRecord(record: bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord) {
        val weight = record.weight
        val heightM = if (record.heightUnit == "cm") {
            (record.heightCm ?: 0f) / 100f
        } else {
            val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
            totalInches * 0.0254f
        }

        val bmi = if (heightM > 0) weight / (heightM * heightM) else 0f
        val gender = if (record.gender == "Male") 0 else 1
        val age = record.age

        binding.bmiGauge.onBmiChangeListener = { animatedValue ->
            binding.tvBmiValue.text = String.format(Locale.US, "%.1f", animatedValue)
        }
        binding.bmiGauge.updateConfig(gender, age)
        binding.bmiGauge.setBmi(bmi)

        val (sections, _) = BmiConfigManager.getConfiguration(gender, age)

        // Find current category
        val currentSection = sections.find {
            bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE)
        } ?: sections.lastOrNull()

        currentSection?.let {
            binding.tvStatus.text = it.categoryName
            try {
                val color = Color.parseColor(it.color)
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            } catch (e: Exception) {}
        }

        // Display input summary
        val heightStr = if (record.heightUnit == "ft+in") {
            "${record.heightFt}'${record.heightIn}\""
        } else {
            "${record.heightCm}cm"
        }
        binding.tvMessage.text = getString(R.string.bmi_input_data, "${record.weight}${record.weightUnit}", heightStr, record.gender, record.age.toString())

        rangeAdapter.setData(sections, bmi)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
