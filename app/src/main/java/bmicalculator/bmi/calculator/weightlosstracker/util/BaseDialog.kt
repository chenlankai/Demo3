package bmicalculator.bmi.calculator.weightlosstracker.util


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import bmicalculator.bmi.calculator.weightlosstracker.R

abstract class BaseDialog(context: Context, val theme: Int = R.style.CommonDialog_Theme) :
    Dialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentView())
        //setContentView(R.layout.xx)
        // 设置window要在setContentView之后
        window?.run {
            // 屏蔽Dialog默认的背景
            setBackgroundDrawable(null)
            //setGravity(Gravity.CENTER)
            setLayout(getDialogWidth(), WindowManager.LayoutParams.WRAP_CONTENT)
            //StatusBarUtil.handleNavigationBar(this,true)
        }

        initView(savedInstanceState)
    }

    abstract fun getContentView(): View

    abstract fun initView(savedInstanceState: Bundle?)

    open fun getDialogWidth() = getScreenWidth(context) - context.dp2px(24f) * 2

    protected fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

}