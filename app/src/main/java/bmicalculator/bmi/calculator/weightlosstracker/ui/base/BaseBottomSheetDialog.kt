package bmicalculator.bmi.calculator.weightlosstracker.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import bmicalculator.bmi.calculator.weightlosstracker.R

abstract class BaseBottomSheetDialog<VB : ViewBinding>(context: Context) :
    BottomSheetDialog(context, R.style.TransparentBottomSheetDialogTheme) {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 自动利用子类实现的 inflateBinding 初始化 Binding
        _binding = inflateBinding(LayoutInflater.from(context))
        // 2. 设置根视图
        setContentView(binding.root)
        // 3. 触发子类的业务初始化
        initDialog()
    }

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    abstract fun initDialog()

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}