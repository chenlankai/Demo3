package bmicalculator.bmi.calculator.weightlosstracker.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import bmicalculator.bmi.calculator.weightlosstracker.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.Locale

class StatisticsChartMarker(context: Context, layoutResource: Int, private val unit: String = "") : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var currentYValue: Float? = null

    override fun refreshContent(e: Entry, highlight: Highlight) {
        currentYValue = e.y

        tvContent.text = if (unit.isEmpty()) {
            String.format(Locale.US, "%.1f", e.y)
        } else {
            // 体重显示 1 位小数
            String.format(Locale.US, "%.1f %s", e.y, unit)
        }

        super.refreshContent(e, highlight)
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        // 关键逻辑：只有 unit 为空（即 BMI 模式）时才绘制彩色圈圈
        if (unit.isEmpty()) {
            val yVal = currentYValue
            if (yVal != null) {
                val bmiColor = getBmiColor(yVal)

                /* 如果需要光晕可以取消注释
                haloPaint.color = bmiColor
                haloPaint.alpha = 70
                canvas.drawCircle(posX, posY, 22f, haloPaint)
                */

                // 绘制白色外圈底色
                dotPaint.color = Color.WHITE
                dotPaint.style = Paint.Style.FILL
                canvas.drawCircle(posX, posY, 15f, dotPaint)

                // 绘制中心彩色圆点
                dotPaint.color = bmiColor
                canvas.drawCircle(posX, posY, 12f, dotPaint)
            }
        }
        else{
            // 绘制白色外圈底色
            dotPaint.color = Color.WHITE
            dotPaint.style = Paint.Style.FILL
            canvas.drawCircle(posX, posY, 15f, dotPaint)
        }

        // 无论哪种模式，都要绘制上方的黑色气泡文字
        super.draw(canvas, posX, posY)
    }

    override fun getOffset(): MPPointF {
        // 如果是体重模式（没有彩色大圈），偏移量可以稍微调小一点，比如 -15f
        // 如果想统一，用 -35f 也可以
        val yOffset = if (unit.isEmpty()) -35f else -15f
        return MPPointF(-(width / 2).toFloat(), -height.toFloat() + yOffset)
    }

    private fun getBmiColor(bmi: Float): Int {
        val colorStr = when {
            bmi < 16f -> "#286DE6"
            bmi in 16f..17f -> "#349CEA"
            bmi in 17f..18.5f -> "#5BB1F5"
            bmi in 18.5f..25f -> "#A8C526"
            bmi in 25f..30f -> "#FECD2E"
            bmi in 30f..35f -> "#FD9845"
            bmi in 35f..40f -> "#F67D3C"
            else -> "#F04E46"
        }
        return Color.parseColor(colorStr)
    }
}