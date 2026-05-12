package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    // 数据包装类
    data class ChartData(
        val entries: List<Entry>,
        val labels: List<String>,
        val titleInfoList: List<String>
    )

    class CustomXAxisRenderer(
        val context: Context,
        viewPortHandler: ViewPortHandler,
        xAxis: XAxis,
        transformer: Transformer,
        private val titleInfoList: List<String>
    ) : XAxisRenderer(viewPortHandler, xAxis, transformer) {

        override fun drawLabel(c: Canvas?, formattedLabel: String?, x: Float, y: Float, anchor: MPPointF?, angleDegrees: Float) {
            // 1. 绘制日期数字 (如 30, 1, 2...)
            super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees)

            // 2. 将像素坐标 x 转换为逻辑上的数据索引
            val pts = floatArrayOf(x, 0f)
            mTrans.pixelsToValue(pts)
            val index = Math.round(pts[0]) // 四舍五入得到最接近的整数索引

            // 3. 只有当该索引对应 titleInfoList 中的“月份”不为空时才绘制
            if (index >= 0 && index < titleInfoList.size) {
                val monthName = titleInfoList[index]
                if (monthName.isNotEmpty()) {
                    val paint = Paint(mAxisLabelPaint).apply {
                        textAlign = Paint.Align.CENTER
                        color = Color.WHITE
                        typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
                        textSize = mAxisLabelPaint.textSize
                    }

                    // 在绘图区顶部偏移 30f 处绘制（镶嵌在内部）
                    val monthY = mViewPortHandler.contentTop() - 30f
                    c?.drawText(monthName, x, monthY, paint)
                }
            }
        }
    }

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

        initChartStyle(binding.bmiChart, "BMI")
        initChartStyle(binding.weightChart, "kg")

        setupTabSystem()
        binding.tvDay.performClick()

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


            // 允许子视图绘制在剪切范围外，确保月份文字可见
            setClipChildren(false)
            setClipToPadding(false)

            // 顶部 Offset 留出 ，给月份文字预留空间
            setExtraOffsets(11f, 29.5f, 20.5f, 17.5f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                textSize = 12f
                setDrawGridLines(true)
                axisLineColor = Color.parseColor("#EEEEEE")
                axisLineWidth = 0.5f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_extrabold)
                axisLineColor = Color.TRANSPARENT
                yOffset = 8f
            }

            axisLeft.apply {
                textColor = Color.WHITE
                textSize = 12f
                setDrawGridLines(false)
                typeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_extrabold)
                axisLineColor = Color.TRANSPARENT
                xOffset = 13f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = String.format("%.1f", value)
                }
            }
            axisRight.isEnabled = false
            marker = StatisticsChartMarker(requireContext(), R.layout.view_chart_marker, if(unit == "BMI") "" else unit)
        }
    }

    private fun renderAll() {
        if (allRecords.isEmpty()) return
        renderSingleChart(binding.bmiChart, true)
        renderSingleChart(binding.weightChart, false)
    }

    private fun renderSingleChart(chart: LineChart, isBmi: Boolean) {
        val dataPair = prepareDataByMode(isBmi)
        val entries = dataPair.entries
        val labels = dataPair.labels
        val titleInfoList = dataPair.titleInfoList

        val maxX = (labels.size - 1).toFloat()

        // --- 应用自定义渲染器 ---
        chart.setXAxisRenderer(CustomXAxisRenderer(
            requireContext(),
            chart.viewPortHandler,
            chart.xAxis,
            chart.getTransformer(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT),
            titleInfoList
        ))

        val startColor = if (isBmi) "#80FFFFFF" else "#80FFFFFF"
        val endColor = if (isBmi) "#00FFFFFF" else "#00FFFFFF"
        chart.data = LineData(createDataSet(entries, startColor, endColor,isBmi))

        // 纵坐标 75% 比例锁定
        if (entries.isNotEmpty()) {
            val minY = entries.minOf { it.y }
            val maxY = entries.maxOf { it.y }
            var delta = maxY - minY
            if (delta < 0.5f) delta = 2.0f

            val totalRange = delta / 0.75f
            val marginTotal = totalRange - delta
            val marginBottom = marginTotal * 0.2f
            val marginTop = marginTotal - marginBottom

            chart.axisLeft.apply {
                axisMinimum = minY - marginBottom
                axisMaximum = maxY + marginTop
                setLabelCount(6, true)
            }
        }

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            axisMinimum = -0.2f // 稍微留白，防止左侧 30 贴边
            axisMaximum = maxX + 0.2f

            granularity = 1f
            isGranularityEnabled = true

            setLabelCount(8, false)

            setAvoidFirstLastClipping(false)
        }
        chart.setVisibleXRangeMaximum(8f)

        if (entries.isNotEmpty()) {
            val lastX = entries.last().x
            val targetX = lastX - 7f
            chart.moveViewToX(targetX)
        } else {
            chart.moveViewToX(labels.size.toFloat())
        }

        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun prepareDataByMode(isBmi: Boolean): ChartData {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        val titleInfoList = mutableListOf<String>()
        if (allRecords.isEmpty()) return ChartData(entries, labels, titleInfoList)

        val cal = Calendar.getInstance()
        cal.time = parseDate(allRecords.last().date) ?: Date()

        when (currentMode) {
            getString(R.string.week), "Week" -> {
                val weekCount = 30 // 想要显示的总周数
                val labelSdf = SimpleDateFormat("d", Locale.getDefault())
                val monthSdf = SimpleDateFormat("MMMM", Locale.getDefault())

                // 锚点：本周周六
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)

                for (i in 0 until weekCount) {
                    val weekEnd = (cal.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, -(weekCount - 1 - i)) }
                    val weekStart = (weekEnd.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, -6)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }

                    labels.add(labelSdf.format(weekStart.time))

                    val currentMonth = weekStart.get(Calendar.MONTH)
                    val isFirstDayOfMonthInWeek = weekStart.get(Calendar.DAY_OF_MONTH) <= 7 && weekStart.get(Calendar.DAY_OF_MONTH) >= 1

                    if (isFirstDayOfMonthInWeek) {
                        titleInfoList.add(monthSdf.format(weekStart.time))
                    } else {
                        titleInfoList.add("")
                    }

                    val weekRecords = allRecords.filter {
                        val d = parseDate(it.date)
                        d != null && d.time >= weekStart.timeInMillis && d.time <= weekEnd.timeInMillis
                    }
                    if (weekRecords.isNotEmpty()) {
                        val avg = weekRecords.map { calculateVal(it, isBmi) }.average().toFloat()
                        entries.add(Entry(i.toFloat(), avg))
                    }
                }
            }
            getString(R.string.month), "Month" -> {
                val monthCount = 36
                val monthSdf = SimpleDateFormat("M", Locale.getDefault())
                val yearSdf = SimpleDateFormat("yyyy", Locale.getDefault())

                for (i in 0 until monthCount) {
                    val targetMonth = (cal.clone() as Calendar).apply {
                        add(Calendar.MONTH, -(monthCount - 1 - i))
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }

                    labels.add(monthSdf.format(targetMonth.time))

                    // 逻辑：如果是 1 月，顶部显示年份
                    if (targetMonth.get(Calendar.MONTH) == Calendar.JANUARY) {
                        titleInfoList.add(yearSdf.format(targetMonth.time))
                    } else {
                        titleInfoList.add("")
                    }

                    val monthStart = targetMonth.time
                    targetMonth.add(Calendar.MONTH, 1)
                    val monthEnd = targetMonth.time

                    val monthRecords = allRecords.filter {
                        val d = parseDate(it.date)
                        d != null && d.time >= monthStart.time && d.time < monthEnd.time
                    }
                    if (monthRecords.isNotEmpty()) {
                        val avg = monthRecords.map { calculateVal(it, isBmi) }.average().toFloat()
                        entries.add(Entry(i.toFloat(), avg))
                    }
                }
            }
            else -> { // Day 模式
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_MONTH, -1)
                }
                val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)

                for (i in 0..daysInMonth) {
                    val currentDay = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
                    labels.add(SimpleDateFormat("d", Locale.getDefault()).format(currentDay.time))
                    titleInfoList.add(if (i == 1) monthName else "")
                }

                allRecords.groupBy { record ->
                    val rDate = parseDate(record.date)
                    if (rDate != null) ((rDate.time - startCal.timeInMillis) / (24 * 3600 * 1000)).toInt() else -1
                }.forEach { (diff, dayRecords) ->
                    if (diff in 0..daysInMonth) {
                        entries.add(Entry(diff.toFloat(), calculateVal(dayRecords.last(), isBmi)))
                    }
                }
            }
        }
        return ChartData(entries, labels, titleInfoList)
    }

    private fun calculateVal(r: BmiRecord, isBmi: Boolean): Float {
        val w = if (r.weightUnit == "lb") r.weight * 0.453592f else r.weight
        if (!isBmi) return w
        val h = if (r.heightUnit == "cm") (r.heightCm ?: 0f) / 100f
        else ((r.heightFt ?: 0) * 12 + (r.heightIn ?: 0)) * 0.0254f
        return if (h > 0) w / (h * h) else 0f
    }

    private fun createDataSet(entries: List<Entry>, sc: String, ec: String,isBmi : Boolean) = LineDataSet(entries, "").apply {
        // 1. 线条：白色实线
        color = Color.WHITE
        lineWidth = 2.5f
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        setDrawValues(false)

        // 2. 开启填充
        setDrawFilled(true)

        // 3. 设置从上到下的渐变
        // 确保 sc 传入的是不透明或半透明白 (如 #80FFFFFF)
        // 确保 ec 传入的是完全透明白 (如 #00FFFFFF)
        val fillGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor(sc), // 顶部
                Color.parseColor(ec)  // 底部
            )
        )
        fillDrawable = fillGradient

        // 4. 这里的 fillAlpha 设为 255，因为透明度我们已经在 sc/ec 中精确控制了
        fillAlpha = 255

        // 5. 圆点设置
        setDrawCircles(true)
        setCircleColor(Color.WHITE)
        circleRadius = 3f
        setDrawCircleHole(false)

        // 6. 交互
        isHighlightEnabled = true
        setDrawHighlightIndicators(false)
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