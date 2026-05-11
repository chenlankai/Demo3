package bmicalculator.bmi.calculator.weightlosstracker.ui.widget

import android.content.Context
import android.widget.TextView
import bmicalculator.bmi.calculator.weightlosstracker.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.Locale

class StatisticsChartMarker(context: Context, layoutResource: Int, private val unit: String = "") : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        tvContent.text = if (unit.isEmpty()) {
            String.format(Locale.US, "%.1f", e.y)
        } else {
            String.format(Locale.US, "%.1f %s", e.y, unit)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() - 10f)
    }
}