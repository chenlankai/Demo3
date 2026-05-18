package bmicalculator.bmi.calculator.weightlosstracker.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseDialog

class DeleteConfirmDialog(
    context: Context,
    private val onDeleteConfirmed: () -> Unit // 👈 1. 暴露删除回调给外部
) : BaseDialog(context) {

    private val binding = DialogDeleteConfirmBinding.inflate(LayoutInflater.from(context))

    override fun getContentView(): View {
        return binding.root
    }

    override fun initView(savedInstanceState: Bundle?) {
        // 取消按钮：直接关闭弹窗
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // 删除按钮：触发外部回调，并关闭弹窗
        binding.deleteButton.setOnClickListener {
            onDeleteConfirmed() // 👈 2. 通知外部去执行真正的删除和跳转逻辑
            dismiss()
        }
    }
}