package bmicalculator.bmi.calculator.weightlosstracker.util
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.*
import android.text.style.MetricAffectingSpan
import android.view.MotionEvent
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import java.util.Locale
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

fun Context.dp2px(dip: Float) = (dip * displayMetrics.density + 0.5f).toInt()

/**
 * 将px转成dp
 */
fun Context.px2dp(px: Int) = px / displayMetrics.density

/**
 * 将sp转成px
 */
fun Context.sp2px(sip: Float) = (sip * displayMetrics.scaledDensity + 0.5f).toInt()

/**
 * 将px转成sp
 */
fun Context.px2sp(px: Int) = px / displayMetrics.scaledDensity
// 将 DP 转换为 PX 的扩展函数
fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

val Context.displayMetrics: DisplayMetrics
    get() = this.resources.displayMetrics

val Context.density : Float
    get() = displayMetrics.density




fun View.systemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(
            v.paddingLeft,
            systemBars.top,      // 顶部 = 状态栏高度
            v.paddingRight,
            systemBars.bottom    // 底部 = 导航栏高度
        )
        insets
    }
}


fun View.systemBarsTopPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(
            v.paddingLeft,
            systemBars.top,      // 顶部 = 状态栏高度
            v.paddingRight,
            v.paddingBottom
        )
        insets
    }
}
fun View.systemBarsBottomPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(
            v.paddingLeft,
            v.paddingTop,
            v.paddingRight,
            systemBars.bottom    // 底部 = 导航栏高度
        )
        insets
    }
}

class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) = applyCustomTypeFace(ds, typeface)
    override fun updateMeasureState(paint: TextPaint) = applyCustomTypeFace(paint, typeface)

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        paint.typeface = tf
    }
}
