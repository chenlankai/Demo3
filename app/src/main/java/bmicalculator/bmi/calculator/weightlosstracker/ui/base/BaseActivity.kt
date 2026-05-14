package bmicalculator.bmi.calculator.weightlosstracker.ui.base

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bmicalculator.bmi.calculator.weightlosstracker.util.LocaleUtils
import bmicalculator.bmi.calculator.weightlosstracker.R

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {

        super.attachBaseContext(LocaleUtils.applyLocale(newBase))
    }
    companion object {
        private var pendingToast: Triple<String, Int, String>? = null

        /**
         * 供子类调用：请求在回到上一个页面时显示弹窗
         */
        fun requestToastOnBack(message: String, iconRes: Int, colorStr: String) {
            pendingToast = Triple(message, iconRes, colorStr)
        }
    }


    override fun onResume() {
        super.onResume()
        // 每次 Activity 恢复可见时检查是否有待显示的“留言”
        pendingToast?.let { (msg, icon, color) ->
            // 延迟 300ms 避开页面转场动画，防止视觉闪烁
            window.decorView.postDelayed({
                if (!isFinishing) {
                    showStatusToast(msg, icon, color)
                    pendingToast = null // 消费后立即清空，防止重复弹出
                }
            }, 300)
        }
    }

    fun showStatusToast(
        message: String,
        iconRes: Int,
        iconColorStr: String,
        duration: Long = 2000
    ) {
        val view = layoutInflater.inflate(R.layout.layout_custom_toast, null)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)

        tvMessage.text = message
        tvMessage.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)

        try {
            val color = Color.parseColor(iconColorStr)
            tvMessage.compoundDrawableTintList = ColorStateList.valueOf(color)
        } catch (e: Exception) {
            tvMessage.compoundDrawableTintList = ColorStateList.valueOf(Color.BLACK)
        }

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            animationStyle = android.R.style.Animation_Dialog
            isOutsideTouchable = true
        }


        popupWindow.showAtLocation(window.decorView, Gravity.TOP, 0, 150)

        view.postDelayed({
            if (!isFinishing && popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }, duration)
    }
}
