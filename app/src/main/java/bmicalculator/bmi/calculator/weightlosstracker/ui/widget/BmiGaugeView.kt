package bmicalculator.bmi.calculator.weightlosstracker.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import kotlin.math.cos
import kotlin.math.sin


class BmiGaugeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var onBmiChangeListener: ((Float) -> Unit)? = null
    var showPointer: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var sections: List<BmiConfigManager.BmiSection> = emptyList()
    private var currentBmi = 0f
    private var minBmi = 15f
    private var maxBmi = 41f


    private var animatedAngle = 180f
    private var pointerAnimator: ValueAnimator? = null

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
    }

    private val pointerDrawable =
        ResourcesCompat.getDrawable(resources, R.drawable.icon_bmi_pointer1, null)

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var sw = 0f
    private val rectF = RectF()

    init {
        // 初始默认配置
        updateConfig(0, 25)
    }

    /**
     * 根据性别和年龄更新配置
     */
    fun updateConfig(gender: Int, age: Int) {
        val config = BmiConfigManager.getConfiguration(gender, age)
        this.sections = config.first
        this.minBmi = config.second.first
        this.maxBmi = config.second.second
        invalidate()
    }
    
    fun resetState() {
        pointerAnimator?.cancel()
        animatedAngle = 180f
        currentBmi = 0f
        onBmiChangeListener?.invoke(0f)
        invalidate()
    }

    fun setBmi(value: Float, animate: Boolean = true) {
        // 1. 指针旋转的目标：必须限制在 minBmi 和 maxBmi 之间
        val clampedBmiForPointer = value.coerceIn(minBmi, maxBmi)

        // 2. 显示的数值：不限制范围，使用原始传入的值
        val targetBmiValueForDisplay = value

        if (!animate) {
            pointerAnimator?.cancel()
            currentBmi = targetBmiValueForDisplay
            // 基于 clamped 值计算角度
            val ratio = (clampedBmiForPointer - minBmi) / (maxBmi - minBmi)
            animatedAngle = 180f + (ratio * 180f)
            onBmiChangeListener?.invoke(currentBmi)
            invalidate()
            return
        }

        // 1. 立即停止旧动画并强制同步重置状态到起点
        // 这一步是消除“跳动感”的关键：确保在进入新一轮绘制前状态已就绪
        resetState()

        // 2. 准备增长动画的目标参数
        // 这里使用 clamped 值来计算指针的最终目标角度
        val ratio = (clampedBmiForPointer - minBmi) / (maxBmi - minBmi)
        val targetAngle = 180f + (ratio * 180f)

        // 3. 直接启动从 0 开始的增长动画
        pointerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float

                // 动画始终基于固定的起点 (180f / 0f) 进行计算
                animatedAngle = 180f + (targetAngle - 180f) * fraction
                // 数值动画：基于原始的 targetBmiValueForDisplay 进行计算
                val frameBmi = 0f + (targetBmiValueForDisplay - 0f) * fraction
                currentBmi = frameBmi

                onBmiChangeListener?.invoke(frameBmi)
                invalidate()
            }
            start()
        }

        // 立即触发重绘，确保哪怕动画第一帧未到，画面也已处于 0 位
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        // 高度计算：外半径(153dp) + 文字空间(25dp) + 底部预留少量间隙
        val h = ((153f + 35f) * density).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        val density = resources.displayMetrics.density

        var outerRadiusPx = 153f * density
        var innerRadiusPx = 69f * density

        // 适配屏幕宽度，留出文字Padding
        val horizontalPadding = 30f * density
        val maxAvailableRadius = (w - horizontalPadding * 2) / 2f

        if (outerRadiusPx > maxAvailableRadius) {
            val scale = maxAvailableRadius / outerRadiusPx
            outerRadiusPx = maxAvailableRadius
            innerRadiusPx *= scale
        }

        centerX = w / 2f
        val topTextSpace = 25f * density
        centerY = outerRadiusPx + topTextSpace

        sw = outerRadiusPx - innerRadiusPx
        radius = (outerRadiusPx + innerRadiusPx) / 2f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rectF.isEmpty || sections.isEmpty()) return

        arcPaint.strokeWidth = sw
        var startAngle = 180f
        val totalWeight = 100f

        for (section in sections) {
            val sweepAngle = (section.weightPercentage / totalWeight) * 180f

            // 1. 绘制刻度标签
            if (section.tickLabel.isNotEmpty()) {
                drawSlantedLabel(canvas, section.tickLabel, startAngle, centerX, centerY, radius, sw)
            }

            // 2. 绘制弧形色块
            arcPaint.color = Color.parseColor(section.gaugeColor)
            canvas.drawArc(rectF, startAngle, sweepAngle + 0.8f, false, arcPaint)

            startAngle += sweepAngle
        }

        // 3. 绘制指针
        if (showPointer) {
            drawIconPointer(canvas, centerX, centerY, radius, sw)
        }
    }

    private fun drawSlantedLabel(canvas: Canvas, text: String, angle: Float, cx: Float, cy: Float, radius: Float, sw: Float) {
        canvas.save()
        val density = resources.displayMetrics.density
        val textRadius = radius + (sw / 2f) + (8f * density)
        val rad = Math.toRadians(angle.toDouble())

        val tx = cx + (textRadius * cos(rad)).toFloat()
        val ty = cy + (textRadius * sin(rad)).toFloat()

        canvas.translate(tx, ty)
        canvas.rotate(angle + 90f)

        textPaint.textSize = 10f * density
        val baseline = -(textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, 0f, baseline, textPaint)
        canvas.restore()
    }

    private fun drawIconPointer(canvas: Canvas, cx: Float, cy: Float, radius: Float, sw: Float) {
        val drawable = pointerDrawable ?: return
        canvas.save()

        // 旋转校准：111.5f 补偿使图片在 animatedAngle=180 时水平向左
        canvas.rotate(animatedAngle + 111.5f, cx, cy)

        val pWidth = drawable.intrinsicWidth.toFloat()
        val pHeight = drawable.intrinsicHeight.toFloat()

        // 基于 Vector (53x92) 的圆心比例定位轴心
        val pivotXPercent = 38.5f / 53f
        val pivotYPercent = 77.6f / 92f

        val pivotXPx = pWidth * pivotXPercent
        val pivotYPx = pHeight * pivotYPercent

        // 计算绘制区域，让图片的圆心轴对齐仪表盘中心 (cx, cy)
        val left = (cx - pivotXPx).toInt()
        val top = (cy - pivotYPx).toInt()
        val right = (left + pWidth).toInt()
        val bottom = (top + pHeight).toInt()

        drawable.setBounds(left, top, right, bottom)
        drawable.draw(canvas)

        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        pointerAnimator?.cancel()
        super.onDetachedFromWindow()
    }
}