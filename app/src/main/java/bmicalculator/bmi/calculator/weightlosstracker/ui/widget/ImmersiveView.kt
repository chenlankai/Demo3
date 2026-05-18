package bmicalculator.bmi.calculator.weightlosstracker.util

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View

class
ImmersiveView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        var statusBarHeight = -1
    }

    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, getStatusBarHeight())
    }

    private fun getStatusBarHeight(): Int {
        if (statusBarHeight != -1) {
            return statusBarHeight
        }

        try {
            statusBarHeight = context.resources
                .getDimensionPixelSize(
                    Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
                )
        } catch (e: Throwable) {

        }

        if (statusBarHeight <= 0) {
            statusBarHeight = context.dp2px(25f)
        }

        return statusBarHeight
    }
}