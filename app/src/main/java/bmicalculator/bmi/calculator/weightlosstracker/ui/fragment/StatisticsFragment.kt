package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentStatisticsBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.MainViewModel
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.StatisticsViewModel
import bmicalculator.bmi.calculator.weightlosstracker.ui.widget.StatisticsChartMarker
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels {
        StatisticsViewModel.Factory(AppDatabase.getDatabase(requireContext()).bmiDao())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        setupListeners()
        observeData()
    }

    private fun setupCharts() {
        configureChart(binding.bmiChart)
        configureChart(binding.weightChart)
        
        binding.bmiChart.marker = StatisticsChartMarker(requireContext(), R.layout.view_chart_marker)
        binding.weightChart.marker = StatisticsChartMarker(requireContext(), R.layout.view_chart_marker, "kg")
    }

    private fun configureChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            extraBottomOffset = 10f

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                textColor = Color.WHITE
                textSize = 12f
                axisLineColor = Color.TRANSPARENT
                yOffset = 10f
                setLabelCount(7, false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                textColor = Color.WHITE
                textSize = 12f
                axisLineColor = Color.TRANSPARENT
                xOffset = 10f
                setLabelCount(6, false)
            }

            axisRight.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.tvUpdateBmi.setOnClickListener { goToCalculator() }
        binding.tvUpdateWeight.setOnClickListener { goToCalculator() }
        
        val tabViews = listOf(binding.tvDay, binding.tvWeek, binding.tvMonth)
        tabViews.forEach { tab ->
            tab.setOnClickListener { selectTab(tab, tabViews) }
        }
    }

    private fun selectTab(selected: View, all: List<View>) {
        all.forEach {
            it.setBackgroundResource(0)
            (it as? android.widget.TextView)?.setTextColor(Color.parseColor("#C7C7CC"))
        }
        selected.setBackgroundResource(R.drawable.bg_statistics_tab_item_selected)
        (selected as? android.widget.TextView)?.setTextColor(Color.BLACK)
    }

    private fun goToCalculator() {
        ViewModelProvider(requireActivity())[MainViewModel::class.java].selectTab(0)
    }

    private fun observeData() {
        viewModel.allRecords.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) return@observe
            val sortedRecords = records.sortedBy { it.date }
            
            // 更新月份文本
            try {
                val lastDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(sortedRecords.last().date)
                val monthName = SimpleDateFormat("MMMM", Locale.US).format(lastDate!!)
                binding.tvBmiMonth.text = monthName
                binding.tvWeightMonth.text = monthName
            } catch (e: Exception) {}

            updateBmiChart(sortedRecords)
            updateWeightChart(sortedRecords)
        }
    }

    private fun updateBmiChart(records: List<BmiRecord>) {
        val entries = records.mapIndexed { index, record ->
            val heightM = if (record.heightUnit == "cm") (record.heightCm ?: 0f) / 100f else ((record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)) * 0.0254f
            val weightKg = if (record.weightUnit == "lb") record.weight * 0.45359237f else record.weight
            val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f
            Entry(index.toFloat(), bmi)
        }

        val dataSet = createDataSet(entries, "#5A7BF0", "#3659CF")
        binding.bmiChart.data = LineData(dataSet)
        binding.bmiChart.xAxis.valueFormatter = IndexAxisValueFormatter(records.map { it.date.takeLast(2) })
        binding.bmiChart.invalidate()
    }

    private fun updateWeightChart(records: List<BmiRecord>) {
        val entries = records.mapIndexed { index, record ->
            val weightKg = if (record.weightUnit == "lb") record.weight * 0.45359237f else record.weight
            Entry(index.toFloat(), weightKg)
        }

        val dataSet = createDataSet(entries, "#F7B26D", "#F09235")
        binding.weightChart.data = LineData(dataSet)
        binding.weightChart.xAxis.valueFormatter = IndexAxisValueFormatter(records.map { it.date.takeLast(2) })
        binding.weightChart.invalidate()
    }

    private fun createDataSet(entries: List<Entry>, startColor: String, endColor: String): LineDataSet {
        return LineDataSet(entries, "").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = Color.WHITE
            setDrawCircles(true)
            setCircleColor(Color.WHITE)
            circleRadius = 5f
            setDrawCircleHole(false)
            lineWidth = 2.5f
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))
            highLightColor = Color.WHITE
            setDrawHorizontalHighlightIndicator(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}