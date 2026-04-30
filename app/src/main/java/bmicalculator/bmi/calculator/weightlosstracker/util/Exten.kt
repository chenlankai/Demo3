package bmicalculator.bmi.calculator.weightlosstracker.util
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.util.DisplayMetrics
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import java.util.Locale
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.text.TextWatcher

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


fun EditText.setupMedicalInput(
    unit: String,
    min: Float,
    max: Float,
    isDecimal: Boolean,
    showUnitDuringEdit: Boolean,
    maxLen: Int
) {
    // 1. 设置字符长度限制
    this.filters = arrayOf(InputFilter.LengthFilter(maxLen))

    // 2. 焦点监听：处理进入/退出编辑状态
    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            // 【进入编辑】
            val currentText = text.toString()
            val digits = currentText.replace(unit, "").trim()
            setText(digits)
            setSelection(text.length)
        } else {

            performValidationAndFormat(this, unit, min, max, isDecimal)
        }
    }


    if (showUnitDuringEdit) {
        this.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val original = s.toString()
                val digits = original.filter { it.isDigit() || it == '.' }

                val result = if (digits.isEmpty()) "" else "$digits$unit"

                if (original != result) {
                    isUpdating = true
                    setText(result)
                    setSelection(digits.length)
                    isUpdating = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    this.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clearFocus() // 触发失去焦点逻辑
            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            true
        } else false
    }
}

private fun performValidationAndFormat(et: EditText, unit: String, min: Float, max: Float, isDecimal: Boolean) {
    val rawInput = et.text.toString().filter { it.isDigit() || it == '.' }
    if (rawInput.isNotEmpty()) {
        var value = rawInput.toFloatOrNull() ?: min

        // 范围检测
        if (value < min) {
            value = min
            Toast.makeText(et.context, "Minimum is $min", Toast.LENGTH_SHORT).show()
        } else if (value > max) {
            value = max
            Toast.makeText(et.context, "Maximum is $max", Toast.LENGTH_SHORT).show()
        }

        val finalStr = if (isDecimal) {
            String.format(Locale.US, "%.2f%s", value, unit)
        } else {
            "${value.toInt()}$unit"
        }
        et.setText(finalStr)
    }
}