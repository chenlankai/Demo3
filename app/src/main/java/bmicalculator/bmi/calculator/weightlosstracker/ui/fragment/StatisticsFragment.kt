package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentStatisticsBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.StatisticsViewModel
import bmicalculator.bmi.calculator.weightlosstracker.ui.widget.StatisticsChartMarker
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels {
        StatisticsViewModel.Factory(AppDatabase.getDatabase(requireContext()).bmiDao())
    }

    private var currentMode = "Day"
    private var allRecords: List<BmiRecord> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEdgeToEdge()

        // 1. 初始化图表样式（重点解决坐标跑出范围）
        initChartStyle(binding.bmiChart, "BMI")
        initChartStyle(binding.weightChart, "kg")

        setupTabSystem()

        viewModel.allRecords.observe(viewLifecycleOwner) { records ->
            if (records.isNullOrEmpty()) return@observe
            allRecords = records.sortedBy { parseDate(it.date)?.time ?: 0L }
            renderAll()
        }
    }

    private fun initChartStyle(chart: LineChart, unit: String) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(false)
            isDragEnabled = true
            isAutoScaleMinMaxEnabled = false


            minOffset = 15f
            setExtraOffsets(11f, 30f, 20.5f, 17.5f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                textSize = 10f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
                typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
                axisLineColor = Color.TRANSPARENT
                // 解决 X 轴首尾文字被切掉的问题
                setAvoidFirstLastClipping(true)
                yOffset = 8f
            }

            axisLeft.apply {
                textColor = Color.WHITE
                textSize = 11f
                setDrawGridLines(false)
                typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
                axisLineColor = Color.TRANSPARENT
                xOffset = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = String.format("%.1f", value)
                }
            }
            axisRight.isEnabled = false
            marker = StatisticsChartMarker(context, R.layout.view_chart_marker, if(unit == "BMI") "" else unit)
        }
    }

    private fun renderAll() {
        if (allRecords.isEmpty()) return
        renderSingleChart(binding.bmiChart, true)
        renderSingleChart(binding.weightChart, false)
    }

    private fun renderSingleChart(chart: LineChart, isBmi: Boolean) {
        val dataPair = prepareDataByMode(isBmi)
        val entries = dataPair.first
        val labels = dataPair.second
        val maxX = (labels.size - 1).toFloat()

        val startColor = if (isBmi) "#5A7BF0" else "#F7B26D"
        val endColor = if (isBmi) "#3659CF" else "#F09235"
        chart.data = LineData(createDataSet(entries, startColor, endColor))

        // --- 核心数学逻辑：解决文字重叠并锁定 75% 绘图区 ---
        if (entries.isNotEmpty()) {
            val minY = entries.minOf { it.y }
            val maxY = entries.maxOf { it.y }
            var delta = maxY - minY

            // 健壮性：防止 delta 极小导致 Y 轴刻度挤爆
            if (delta < 0.5f) delta = 2.0f

            // 75% 绘图区与 0.8:1 留白比计算
            val totalRange = delta / 0.75f
            val marginTotal = totalRange - delta
            val marginBottom = marginTotal * (0.8f / 1.8f)
            val marginTop = marginTotal - marginBottom

            chart.axisLeft.apply {
                axisMinimum = minY - marginBottom
                axisMaximum = maxY + marginTop
                setLabelCount(6, true) // 强制 6 个刻度，保证布局极其稳定
            }
        }

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            // 给 X 轴左右留出 0.5 的间隙，防止端点贴墙
            axisMinimum = -0.5f
            axisMaximum = maxX + 0.5f
            setLabelCount(if (currentMode == "Day") 7 else 6, false)
        }

        chart.setVisibleXRangeMaximum(if (currentMode == "Day") 7f else 6f)
        chart.moveViewToX(maxX)

        // 月份同步处理 (不改 XML 显隐，通过 translation 移动)
        val monthTv = if (isBmi) binding.tvBmiMonth else binding.tvWeightMonth
        if (currentMode == "Day") {
            monthTv.text = SimpleDateFormat("MMMM", Locale.getDefault()).format(parseDate(allRecords.last().date) ?: Date())
            // 初始同步
            chart.post { syncMonthPosition(chart, monthTv) }
            attachGestureListener(chart, monthTv)
        } else {
            monthTv.visibility = View.INVISIBLE
            chart.onChartGestureListener = null
        }

        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun syncMonthPosition(chart: LineChart, monthView: TextView) {
        if (monthView.width == 0) return

        // 计算 X = 1 (即1号) 在屏幕上的物理像素位置
        val pts = floatArrayOf(1f, 0f)
        chart.getTransformer(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT).pointValuesToPixel(pts)

        val contentL = chart.viewPortHandler.contentLeft()
        val contentR = chart.viewPortHandler.contentRight()

        // 仅在 1 号可见时显示，使用 translationX 移动不影响布局稳定性
        if (pts[0] in contentL..contentR) {
            monthView.visibility = View.VISIBLE
            // pts[0] 是绝对屏幕位置，需要减去 TextView 中心偏移
            monthView.translationX = pts[0] - (monthView.width / 2f)
        } else {
            monthView.visibility = View.INVISIBLE
        }
    }

    private fun attachGestureListener(chart: LineChart, monthView: TextView) {
        chart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = syncMonthPosition(chart, monthView)
            override fun onChartScale(me: MotionEvent?, sX: Float, sY: Float) = syncMonthPosition(chart, monthView)
            override fun onChartGestureStart(me: MotionEvent?, lastGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartGestureEnd(me: MotionEvent?, lastGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartSingleTapped(me: MotionEvent?) {}
            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, vX: Float, vY: Float) = syncMonthPosition(chart, monthView)
        }
    }

    private fun prepareDataByMode(isBmi: Boolean): Pair<List<Entry>, List<String>> {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        if (allRecords.isEmpty()) return Pair(entries, labels)

        val cal = Calendar.getInstance()
        cal.time = parseDate(allRecords.last().date) ?: Date()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.DAY_OF_MONTH, -1) }

        for (i in 0..daysInMonth) {
            val d = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }.time
            labels.add(SimpleDateFormat("d", Locale.getDefault()).format(d))
        }

        allRecords.forEach { record ->
            val rDate = parseDate(record.date) ?: return@forEach
            val diff = ((rDate.time - startCal.timeInMillis) / (24 * 3600 * 1000)).toInt()
            if (diff in 0..daysInMonth) {
                entries.add(Entry(diff.toFloat(), calculateVal(record, isBmi)))
            }
        }
        return Pair(entries, labels)
    }

    private fun calculateVal(r: BmiRecord, isBmi: Boolean): Float {
        val w = if (r.weightUnit == "lb") r.weight * 0.453592f else r.weight
        if (!isBmi) return w
        val h = if (r.heightUnit == "cm") (r.heightCm ?: 0f) / 100f
        else ((r.heightFt ?: 0) * 12 + (r.heightIn ?: 0)) * 0.0254f
        return if (h > 0) w / (h * h) else 0f
    }

    private fun createDataSet(entries: List<Entry>, sc: String, ec: String) = LineDataSet(entries, "").apply {
        mode = LineDataSet.Mode.CUBIC_BEZIER
        color = Color.WHITE
        lineWidth = 2.5f
        setDrawCircles(true)
        setCircleColor(Color.WHITE)
        circleRadius = 4f
        setDrawCircleHole(false)
        setDrawValues(false)
        setDrawFilled(true)
        fillDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(sc), Color.parseColor(ec)))
        setDrawHorizontalHighlightIndicator(false)
    }

    private fun setupTabSystem() {
        val tabs = listOf(binding.tvDay, binding.tvWeek, binding.tvMonth)
        tabs.forEach { tab ->
            tab.setOnClickListener {
                tabs.forEach { it.alpha = 0.5f; it.setBackgroundResource(0); (it as TextView).setTextColor(Color.BLACK) }
                tab.alpha = 1.0f
                tab.setBackgroundResource(R.drawable.bg_statistics_tab_item_selected)
                currentMode = tab.text.toString()
                renderAll()
            }
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutToolbar.updatePadding(top = bars.top)
            insets
        }
    }

    private fun parseDate(s: String) = try { SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(s) } catch (e: Exception) { null }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}