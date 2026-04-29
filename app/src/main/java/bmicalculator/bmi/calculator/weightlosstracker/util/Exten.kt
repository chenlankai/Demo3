package bmicalculator.bmi.calculator.weightlosstracker.util

import android.content.Context
import android.util.DisplayMetrics

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

val Context.displayMetrics: DisplayMetrics
    get() = this.resources.displayMetrics

val Context.density : Float
    get() = displayMetrics.density