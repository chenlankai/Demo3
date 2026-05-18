package bmicalculator.bmi.calculator.weightlosstracker.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseBottomSheetDialog
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogTimePickerBinding

class TimePickerDialog(
    context: Context,
    private val currentKey: String,
    private val timeKeys: List<String>,
    private val timeLabels: List<String>,
    private val onTimeSelected: (String) -> Unit
) : BaseBottomSheetDialog<DialogTimePickerBinding>(context) {

    override fun inflateBinding(inflater: LayoutInflater): DialogTimePickerBinding {
        return DialogTimePickerBinding.inflate(inflater)
    }

    // ✨ 将原先的 onStart 替换为 initDialog 承载 Picker 绑定和点击事件
    override fun initDialog() {

        // 1. 匹配当前已选中的初始位置
        val initialPosition = timeKeys.indexOf(currentKey).coerceAtLeast(0)
        binding.timePicker.setData(timeLabels, initialPosition)

        // 2. 监听按钮点击
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener {
            val selectedKey = timeKeys[binding.timePicker.selectedPosition]
            // 将结果通过 Lambda 表达式回调回去
            onTimeSelected(selectedKey)
            dismiss()
        }
    }
}