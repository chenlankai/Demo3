package bmicalculator.bmi.calculator.weightlosstracker.util
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.text.*
import android.view.MotionEvent
import android.util.DisplayMetrics
import android.view.Gravity
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

fun EditText.setupMedicalInput(
    unit: String,
    min: Float,
    max: Float,
    decimalDigits: Int,
    showUnitDuringEdit: Boolean,
    maxLen: Int

) {
    require(decimalDigits in 0..2) { "decimalDigits must be 0, 1, or 2" }

    if (!showUnitDuringEdit) {
        this.filters = arrayOf(InputFilter.LengthFilter(maxLen))
        this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val rawInput = text.toString().filter { it.isDigit() || it == '.' }
                val value1 = rawInput.toFloatOrNull()
                val isValid = value1 != null && value1 in min..max
                if (!isValid) {
                    setText("$min")
                    setSelection(0)
                    val message = if (unit == "kg" || unit == "lb") {
                        "Please input a valid weight ($min - $max $unit) to calculate your BMI accurately"
                    } else {
                        "Please input a valid Height ($min - $max $unit) to calculate your BMI accurately"
                    }
                    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0, 50)
                    toast.show()
                    return@OnFocusChangeListener
                }
                val value = if (rawInput.isEmpty()) min else rawInput.toFloatOrNull() ?: min
                val clamped = value.coerceIn(min, max)
                val format = when (decimalDigits) {
                    0 -> "%.0f"
                    1 -> "%.1f"
                    else -> "%.2f"
                }
                val formatted = String.format(Locale.US, format, clamped)
                setText(formatted)
            }
        }
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)
                true
            } else false
        }
        return
    }

    val selectionWatcher = object : SpanWatcher {
        private var isSelecting = false
        override fun onSpanAdded(text: Spannable?, span: Any?, start: Int, end: Int) {}
        override fun onSpanRemoved(text: Spannable?, span: Any?, start: Int, end: Int) {}
        override fun onSpanChanged(text: Spannable?, span: Any?, ostart: Int, oend: Int, nstart: Int, nend: Int) {
            if (isSelecting || text == null) return
            if (span === Selection.SELECTION_START || span === Selection.SELECTION_END) {
                val fullText = text.toString()
                if (fullText.endsWith(unit)) {
                    val digitLen = (fullText.length - unit.length).coerceAtLeast(0)
                    val selStart = Selection.getSelectionStart(text)
                    val selEnd = Selection.getSelectionEnd(text)
                    if (selStart > digitLen || selEnd > digitLen) {
                        isSelecting = true
                        Selection.setSelection(text, selStart.coerceAtMost(digitLen), selEnd.coerceAtMost(digitLen))
                        isSelecting = false
                    }
                }
            }
        }
    }

    fun attachSelectionWatcher() {
        val s = text as? Spannable ?: return
        val oldWatcher = getTag(bmicalculator.bmi.calculator.weightlosstracker.R.id.tag_medical_watcher) as? SpanWatcher
        if (oldWatcher != null) {
            s.removeSpan(oldWatcher)
        }
        s.setSpan(selectionWatcher, 0, s.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        setTag(bmicalculator.bmi.calculator.weightlosstracker.R.id.tag_medical_watcher, selectionWatcher)
    }

    if (text.isNullOrEmpty()) {
        setText("$min$unit")
        setSelection(0)
    } else if (!text.toString().endsWith(unit)) {
        append(unit)
    }
    attachSelectionWatcher()

    filters = arrayOf(InputFilter.LengthFilter(maxLen + unit.length))

    var isUpdating = false
    val oldTextWatcher = getTag(bmicalculator.bmi.calculator.weightlosstracker.R.id.tag_medical_text_watcher) as? TextWatcher
    if (oldTextWatcher != null) {
        removeTextChangedListener(oldTextWatcher)
    }

    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (isUpdating) return

            val original = s.toString()
            var digits = if (original.endsWith(unit)) {
                original.substring(0, original.length - unit.length)
            } else {
                original
            }
            digits = digits.filter { it.isDigit() || it == '.' }
            if (digits.count { it == '.' } > 1) {
                val parts = digits.split('.')
                digits = parts[0] + "." + parts.drop(1).joinToString("").replace(".", "")
            }
            if (decimalDigits == 0 && digits.contains('.')) {
                digits = digits.substringBefore('.')
            }
            if (digits.length > maxLen) {
                digits = digits.substring(0, maxLen)
            }

            val newText = if (digits.isEmpty()) unit else "$digits$unit"

            if (original != newText) {
                isUpdating = true
                setText(newText)
                attachSelectionWatcher()
                setSelection(if (digits.isNotEmpty()) digits.length else 0)
                isUpdating = false
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    addTextChangedListener(textWatcher)
    setTag(bmicalculator.bmi.calculator.weightlosstracker.R.id.tag_medical_text_watcher, textWatcher)


    setOnTouchListener { v, event ->
        val et = v as EditText
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val fullText = et.text.toString()
            if (fullText.endsWith(unit)) {
                val layout = et.layout
                if (layout != null) {
                    val x = event.x - et.totalPaddingLeft + et.scrollX
                    val y = event.y - et.totalPaddingTop + et.scrollY
                    val line = layout.getLineForVertical(y.toInt())
                    val offset = layout.getOffsetForHorizontal(line, x)
                    val digitLen = (fullText.length - unit.length).coerceAtLeast(0)
                    
                    if (offset > digitLen) {
                        et.requestFocus()
                        et.setSelection(digitLen)
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
                        return@setOnTouchListener true
                    }
                }
            }
        }
        false
    }

    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            val fullText = text.toString()
            if (!fullText.endsWith(unit)) return@OnFocusChangeListener
            val numStr = fullText.substring(0, fullText.length - unit.length).trim()
            if (numStr.isEmpty()) {

                setText("$min$unit")
                attachSelectionWatcher()
                setSelection(0)
                if (unit == "kg" || unit == "lb")
                {
                    val toast = Toast.makeText(context, "Please input a valid weight ($min - $max $unit)  to calculate your BMI accurately", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0, 50)
                    toast.show()
                }
                else{
                    val toast = Toast.makeText(context, "Please input a valid Height ($min - $max $unit)  to calculate your BMI accurately", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0, 50)
                    toast.show()
                }
                return@OnFocusChangeListener

            }


            val value1 = numStr.toFloatOrNull()
            val isValid = value1 != null && value1 in min..max
            if (!isValid) {

                setText("$min$unit")
                attachSelectionWatcher()
                setSelection(0)
                val message = if (unit == "kg" || unit == "lb") {
                    "Please input a valid weight ($min - $max $unit) to calculate your BMI accurately"
                } else {
                    "Please input a valid Height ($min - $max $unit) to calculate your BMI accurately"
                }
                val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP, 0, 50)
                toast.show()
                return@OnFocusChangeListener
            }
            val value = (numStr.toFloatOrNull() ?: min).coerceIn(min, max)
            val formatStr = when (decimalDigits) {
                0 -> "%.0f"
                1 -> "%.1f"
                else -> "%.2f"
            }
            val formattedNum = String.format(Locale.US, formatStr, value)
            val newText = "$formattedNum$unit"
            if (fullText != newText) {
                setText(newText)
                attachSelectionWatcher()
                setSelection(formattedNum.length)
            }
        } else {
            val fullText = text.toString()
            if (fullText.endsWith(unit)) {
                setSelection((fullText.length - unit.length).coerceAtLeast(0))
            }
        }
    }

    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            true
        } else false
    }
}

