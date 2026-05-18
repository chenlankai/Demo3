package bmicalculator.bmi.calculator.weightlosstracker.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseBottomSheetDialog
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogBmiInfoBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.BmiResultViewModel.BmiResultState
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BmiInfoDialog(
    context: Context,
    private val state: BmiResultState
) : BaseBottomSheetDialog<DialogBmiInfoBinding>(context) {

    override fun inflateBinding(inflater: LayoutInflater): DialogBmiInfoBinding {
        return DialogBmiInfoBinding.inflate(inflater)
    }

    // 全局初始化
    override fun initDialog() {

        // 1. 配置 behavior 默认全屏展开（在 setContentView 之后即可直接配置）
        behavior.apply {
            this.state = BottomSheetBehavior.STATE_EXPANDED
            this.skipCollapsed = true
        }

        // 2. 利用 ViewBinding 直接配置控件状态
        binding.bmiGauge.showPointer = false
        binding.bmiGauge.updateConfig(state.gender, state.age)
        binding.bmiGauge.setBmi(state.bmi, false)

        // 3. 配置 RecyclerView
        val rangeAdapter = BmiRangeAdapter()
        binding.rvStatus.layoutManager = LinearLayoutManager(context)
        binding.rvStatus.adapter = rangeAdapter
        rangeAdapter.setData(state.sections, state.bmi)

        // 4. 设置文本与控制显示/隐藏逻辑
        val genderStr = if (state.gender == 0) context.getString(R.string.male) else context.getString(R.string.female)
        binding.tvBmiTipValue.text = context.getString(R.string.bmi_teenager_info_tip, state.age.toString(), genderStr)

        binding.btnGotIt.setOnClickListener { dismiss() }

        if (state.age > 20) {
            binding.tvBmiTip.text = context.getString(R.string.bmi_adult_tip)
            binding.tvBmiTipValue.visibility = View.GONE
        } else {
            binding.tvBmiTip.text = context.getString(R.string.bmi_teenager_tip)
            binding.tvBmiTipValue.visibility = View.VISIBLE
        }
    }
}