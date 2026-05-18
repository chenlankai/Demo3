package bmicalculator.bmi.calculator.weightlosstracker.ui.dialog


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseBottomSheetDialog
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ViewGoogleAccountDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GoogleAccountDialog(
    context: Context,
    private val isLoggedIn: Boolean,
    private val onLogClick: (willLogIn: Boolean) -> Unit
) : BaseBottomSheetDialog<ViewGoogleAccountDialogBinding>(context) {

    override fun inflateBinding(inflater: LayoutInflater): ViewGoogleAccountDialogBinding {
        return ViewGoogleAccountDialogBinding.inflate(inflater)
    }

    override fun initDialog() {
        behavior.apply {
            isDraggable = false
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        // 2. 初始化对话框 UI 状态
        if (isLoggedIn) {
            binding.btnLog.text = "Log out"
            binding.btnLog.setTextColor(Color.parseColor("#F4333C"))
        } else {
            binding.btnLog.text = "Log in"
            binding.btnLog.setTextColor(Color.BLACK)
        }

        // 3. 设置点击事件
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnLog.setOnClickListener {
            // 把“接下来是要登录还是登出”的动作状态通知给宿主
            onLogClick(!isLoggedIn)
            dismiss()
        }
    }
}