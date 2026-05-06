package bmicalculator.bmi.calculator.weightlosstracker.ui.widget

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.core.content.res.ResourcesCompat
import bmicalculator.bmi.calculator.weightlosstracker.R
import kotlin.math.abs
import kotlin.math.roundToInt

class WheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    // 设计参数
    private val VISIBLE_ITEMS = 7
    private val ITEM_HEIGHT_DP = 31f
    private val PADDING_VERTICAL_DP = 15f
    private val TEXT_SIZE_SP = 14f

    var itemHeight = (ITEM_HEIGHT_DP * density).toInt()
    private val verticalPadding = (PADDING_VERTICAL_DP * density).toInt()
    var textSize = TEXT_SIZE_SP * scaledDensity
    
    var textColorNormal = Color.parseColor("#999999")
    var textColorSelected = Color.BLACK

    var items: List<String> = emptyList()
    var selectedPosition = 0
    var onItemSelected: ((Int) -> Unit)? = null

    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var offsetY = 0f
    private val scroller = OverScroller(context)
    private var isDragging = false

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            isDragging = true
            if (items.isEmpty()) return true
            val maxOffset = selectedPosition * itemHeight.toFloat()
            val minOffset = -(items.size - 1 - selectedPosition) * itemHeight.toFloat()
            offsetY = (offsetY - distanceY).coerceIn(minOffset, maxOffset)
            invalidate()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            isDragging = false
            if (items.isEmpty()) return true
            val maxOffset = selectedPosition * itemHeight.toFloat()
            val minOffset = -(items.size - 1 - selectedPosition) * itemHeight.toFloat()
            scroller.fling(0, offsetY.toInt(), 0, velocityY.toInt(), 0, 0, minOffset.toInt(), maxOffset.toInt())
            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            isDragging = true
            parent.requestDisallowInterceptTouchEvent(true)
            if (!scroller.isFinished) scroller.forceFinished(true)
            return true
        }
    })

    init {
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        try {
            paint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
        } catch (e: Exception) {}
    }

    fun setData(data: List<String>, initialPosition: Int = 0) {
        val sameData = this.items == data
        val samePos = this.selectedPosition == initialPosition
        
        if (sameData && samePos) return
        
        if (sameData && !samePos) {
            if (!isDragging && scroller.isFinished) {
                this.selectedPosition = initialPosition.coerceIn(0, data.size - 1)
                this.offsetY = 0f
                invalidate()
            }
            return
        }

        this.items = data
        this.selectedPosition = initialPosition.coerceIn(0, data.size - 1)
        this.offsetY = 0f
        if (!scroller.isFinished) scroller.forceFinished(true)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val totalHeight = itemHeight * VISIBLE_ITEMS + verticalPadding * 2
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(width, totalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (items.isEmpty() || itemHeight <= 0) return

        val center = height / 2f
        val maxOffset = selectedPosition * itemHeight.toFloat()
        val minOffset = -(items.size - 1 - selectedPosition) * itemHeight.toFloat()
        val clampedOffsetY = offsetY.coerceIn(minOffset, maxOffset)

        for (i in items.indices) {
            val itemY = center + (i - selectedPosition) * itemHeight + clampedOffsetY
            
            if (itemY < -itemHeight || itemY > height + itemHeight) continue

            val distance = abs(itemY - center)
            val scale = (1 - distance / (itemHeight * 3.5f)).coerceIn(0f, 1f)
            val alpha = (255 * scale).toInt()

            paint.color = if (distance < itemHeight / 2f) textColorSelected else textColorNormal
            paint.alpha = alpha

            val textBaseline = itemY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], width / 2f, textBaseline, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isDragging = false
            if (scroller.isFinished) startSnap()
        }
        return handled
    }

    private fun startSnap() {
        if (items.isEmpty()) return
        val movedItems = (offsetY / itemHeight).roundToInt()
        val targetPosition = (selectedPosition - movedItems).coerceIn(0, items.size - 1)
        val targetOffsetY = (selectedPosition - targetPosition) * itemHeight
        scroller.startScroll(0, offsetY.toInt(), 0, (targetOffsetY - offsetY).toInt(), 200)
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            offsetY = scroller.currY.toFloat()
            if (items.isNotEmpty()) {
                val maxOffset = selectedPosition * itemHeight.toFloat()
                val minOffset = -(items.size - 1 - selectedPosition) * itemHeight.toFloat()
                offsetY = offsetY.coerceIn(minOffset, maxOffset)
            }
            invalidate()
        } else if (!isDragging && abs(offsetY) > 0.1f) {
            val movedItems = (offsetY / itemHeight).roundToInt()
            val newPos = (selectedPosition - movedItems).coerceIn(0, items.size - 1)
            selectedPosition = newPos
            offsetY = 0f
            onItemSelected?.invoke(selectedPosition)
            invalidate()
        }
    }
}
