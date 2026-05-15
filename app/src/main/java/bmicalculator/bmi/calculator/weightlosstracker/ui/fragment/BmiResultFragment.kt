package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentBmiResultBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.MainActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.RecommendAppAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.BmiResultViewModel
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.MainViewModel
import bmicalculator.bmi.calculator.weightlosstracker.util.CustomTypefaceSpan
import java.util.*
import androidx.activity.OnBackPressedCallback

class BmiResultFragment : Fragment() {

    private var _binding: FragmentBmiResultBinding? = null
    private val binding get() = _binding!!
    private val rangeAdapter = BmiRangeAdapter()
    private lateinit var recommendAdapter: RecommendAppAdapter

    private val viewModel: BmiResultViewModel by viewModels {
        BmiResultViewModel.Factory(AppDatabase.getDatabase(requireContext()).bmiDao())
    }

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.Factory(AppDatabase.getDatabase(requireContext()).bmiDao())
    }

    private val regularTypeface by lazy { ResourcesCompat.getFont(requireContext(), R.font.montserrat_regular) }
    private val extraBoldTypeface by lazy { ResourcesCompat.getFont(requireContext(), R.font.montserrat_extrabold) }
    private val colorBlack = Color.BLACK
    private val colorRed = Color.RED

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBmiResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (binding.layoutToolbar.isVisible) {
                binding.layoutToolbar.updatePadding(top = systemBars.top)
                v.updatePadding(top = 0)
            } else {
                v.updatePadding(top = systemBars.top)
            }
            insets
        }*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDeleteConfirmDialog()
            }
        })

        val hideDescription = arguments?.getBoolean("hide_description", false) ?: false
        binding.tvDescription.isVisible = !hideDescription

        setupRecyclerView()
        setupRecommendRecyclerView()
        setupListeners()
        observeViewModel()
        
        viewModel.loadData(arguments)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
            state.currentSection?.let { section ->
                mainViewModel.updateBmiData(state.bmi, section.categoryResId)
            }
        }

        // 观察 MainViewModel 中的最新记录，用于 Tab 模式下的实时更新
        mainViewModel.latestRecord.observe(viewLifecycleOwner) { record ->
            val args = arguments
            if (args == null || !args.containsKey(BmiResultActivity.EXTRA_BMI)) {
                // 如果是 Tab 模式（没有传入特定的计算结果），则根据数据库最新记录更新 UI
                if (record != null) {
                    viewModel.loadData(null) // 触发 ViewModel 重新计算状态
                } else {
                    updateUI(BmiResultViewModel.BmiResultState(hasDatabaseRecords = false))
                }
            }
        }
    }

    private fun updateUI(state: BmiResultViewModel.BmiResultState) {
        if (state.isStandaloneMode) {
            setupStandaloneUI(state)
        } else {
            setupTabUI(state)
        }

        displayBmiResult(state)
    }

    private fun setupStandaloneUI(state: BmiResultViewModel.BmiResultState) {
        binding.ActionBar1.isVisible = !state.isHistoryMode
        binding.ActionBar2.isVisible = false
        binding.ActionBar3.isVisible = state.isHistoryMode

        binding.tvSave.isVisible = !state.isHistoryMode
        binding.layoutToolbar.isVisible = true
        binding.tvDescription.isVisible = true
        binding.tabClickOverlay.isVisible = false

        if (state.isHistoryMode) {
            binding.rvStatus.isVisible = false
            binding.DivideLine.isVisible = true
            binding.tvNeedApp.isVisible = true
            binding.rvRecommend.isVisible = true
            binding.tvDateTime.isVisible = true
            
            val localizedTime = when (state.time) {
                "Morning" -> getString(R.string.morning)
                "Afternoon" -> getString(R.string.afternoon)
                "Evening" -> getString(R.string.evening)
                "Night" -> getString(R.string.night)
                else -> state.time
            }
            binding.tvDateTime.text = "${state.date} $localizedTime"
        } else {
            val hasRecords = state.hasDatabaseRecords
            binding.rvStatus.isVisible = !hasRecords
            binding.DivideLine.isVisible = hasRecords
            binding.tvNeedApp.isVisible = hasRecords
            binding.rvRecommend.isVisible = hasRecords
            binding.tvDateTime.isVisible = false
        }
    }

    private fun setupTabUI(state: BmiResultViewModel.BmiResultState) {
        binding.ActionBar1.isVisible = false
        binding.ActionBar2.isVisible = true
        binding.ActionBar3.isVisible = false
        binding.tvSave.isVisible = false
        binding.layoutToolbar.isVisible = true
        binding.tvDescription.isVisible = false
        binding.DivideLine.isVisible = false
        binding.tvNeedApp.isVisible = false
        binding.rvRecommend.isVisible = false
        binding.tvDateTime.isVisible = false

        if (state.recordId != -1L || state.bmi > 0) {
            binding.scrollView.isVisible = true
            binding.tvToolbarDate.text = state.date
            binding.tabClickOverlay.isVisible = true
        } else {
            binding.scrollView.isVisible = false
            binding.tabClickOverlay.isVisible = false
        }
        binding.root.requestApplyInsets()
    }

    private fun displayBmiResult(state: BmiResultViewModel.BmiResultState) {
        binding.bmiGauge.onBmiChangeListener = { animatedValue ->
            binding.tvBmiValue.text = String.format(Locale.US, "%.1f", animatedValue)
        }
        binding.bmiGauge.updateConfig(state.gender, state.age)
        // 仅在计算结果的独立模式且非历史查看状态下显示动画，在 Tab 切换或查看记录时直接定位
        val shouldAnimate = state.isStandaloneMode && !state.isHistoryMode
        binding.bmiGauge.setBmi(state.bmi, shouldAnimate)

        val shouldHelpCircle =  state.isStandaloneMode || state.isHistoryMode

        if (shouldHelpCircle && state.hasDatabaseRecords) {
            binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.help_circle, 0)
        } else {
            binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        state.currentSection?.let {
            //binding.tvStatus.text = it.categoryName
            binding.tvStatus.setText(it.categoryResId)
            try {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(it.color))
            } catch (e: Exception) {}
        }

        val genderStr = if (state.gender == 0) getString(R.string.male) else getString(R.string.female)
        val weightStrFormatted = String.format(Locale.US, "%.2f", state.weightVal)
        val hVal1StrFormatted = if (state.hUnit == "cm") String.format(Locale.US, "%.1f", state.hVal1) else String.format(Locale.US, "%.0f", state.hVal1)
        val hVal2StrFormatted = state.hVal2.toString()

        val heightStrForMsg = if (state.hUnit == "ft+in") {
            getString(R.string.bmi_height_ft_in_format, hVal1StrFormatted, hVal2StrFormatted)
        } else {
            getString(R.string.bmi_height_cm_format, hVal1StrFormatted)
        }
        binding.tvMessage.text = getString(R.string.bmi_input_data, "$weightStrFormatted ${state.weightUnit}", heightStrForMsg, genderStr, state.age.toString())

        val shortMessage = getString(R.string.bmi_range_normal_adult_description)
        //val isNormal = state.currentSection?.categoryName.equals("Normal", ignoreCase = true)
        val isNormal = state.currentSection?.categoryResId == R.string.bmi_normal

        binding.tvDescription.text = if (isNormal) {
            buildSpannedString {
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) { append(shortMessage) }
            }
        } else {

            val rangeStart = getString(R.string.bmi_result_suggest_start, heightStrForMsg)

            val rangeText = getString(R.string.bmi_range_format, state.minIdealWeight, state.weightUnit, state.maxIdealWeight, state.weightUnit)
            buildSpannedString {
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) { append("$rangeStart ") }
                inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorBlack)) { append(rangeText) }
                when {
                    state.weightVal > state.maxIdealWeight -> {
                        val loseText = getString(R.string.bmi_weight_lose_format, state.weightVal - state.maxIdealWeight, state.weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) { append(loseText) }
                    }
                    state.weightVal < state.minIdealWeight -> {
                        val gainText = getString(R.string.bmi_weight_gain_format, state.minIdealWeight - state.weightVal, state.weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) { append(gainText) }
                    }
                }
            }
        }
        rangeAdapter.setData(state.sections, state.bmi)
    }

    private fun setupRecyclerView() {
        binding.rvStatus.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rangeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupRecommendRecyclerView() {
        val recommendApps = listOf(
            RecommendAppAdapter.RecommendAppModel(
                "Home Workout - No Equipments",
                "Weight Loss, Lose Belly Fat",
                R.drawable.icon_recommend_s5,
                "4.8",
                "homeworkout.homeworkouts.noequipment"
            ),
            RecommendAppAdapter.RecommendAppModel(
                "Home Workout - No Equipments",
                "Weight Loss, Lose Belly Fat",
                R.drawable.icon_recommend_s5,
                "4.8",
                "homeworkout.homeworkouts.noequipment"
            ),
            RecommendAppAdapter.RecommendAppModel(
                "Home Workout - No Equipments",
                "Weight Loss, Lose Belly Fat",
                R.drawable.icon_recommend_s5,
                "4.8",
                "homeworkout.homeworkouts.noequipment"
            )
        )
        recommendAdapter = RecommendAppAdapter(recommendApps)
        binding.rvRecommend.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        // 使用 OnTouchListener 处理全局点击，并排除 "Recent" 按钮
        binding.tabClickOverlay.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val state = viewModel.uiState.value
                if (state != null && !state.isStandaloneMode) {
                    val rect = android.graphics.Rect()
                    binding.tvRecent.getGlobalVisibleRect(rect)
                    if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        mainViewModel.selectTab(0)
                        return@setOnTouchListener true
                    } else {
                        // 如果点击在 Recent 按钮范围内，触发 Recent 的点击事件
                        binding.tvRecent.performClick()
                        return@setOnTouchListener true
                    }
                }
            }
            true // 消耗事件，防止传到下面
        }

        binding.ivActionBar3ArrowLeft.setOnClickListener {
            requireActivity().finish()
        }
        binding.tvActionBar3Delete.setOnClickListener {
            showDeleteConfirmDialog()
        }
        binding.tvDiscard.setOnClickListener {
            showDeleteConfirmDialog()
        }

        binding.tvSave.setOnClickListener {
            saveBmiRecord()
        }

        binding.tvRecent.setOnClickListener {
            val state = viewModel.uiState.value
            if (state?.hasDatabaseRecords == true) {
                startActivity(Intent(requireContext(), bmicalculator.bmi.calculator.weightlosstracker.ui.activity.HistoryActivity::class.java))
            } else {

                startActivity(Intent(requireContext(), bmicalculator.bmi.calculator.weightlosstracker.ui.activity.DataInputActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                requireActivity().finish()
            }
        }

        binding.tvStatus.setOnClickListener {
            val state = viewModel.uiState.value ?: return@setOnClickListener
            val hasIcon = (state.isStandaloneMode || state.isHistoryMode) && state.hasDatabaseRecords

            if (!state.isStandaloneMode) {
                ViewModelProvider(requireActivity())[MainViewModel::class.java].selectTab(0)
            } else if (hasIcon) {
                showBmiInfoDialog()
            }
        }
    }

    private fun showBmiInfoDialog() {
        val state = viewModel.uiState.value ?: return
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_bmi_info, null)
        dialog.setContentView(view)
        dialog.behavior.apply {
            this.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            this.skipCollapsed = true
        }
        // 设置弹窗中的数据
        val gaugeView = view.findViewById<bmicalculator.bmi.calculator.weightlosstracker.ui.widget.BmiGaugeView>(R.id.bmiGauge)
        val rvStatus = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvStatus)
        val tvTip = view.findViewById<android.widget.TextView>(R.id.tv_bmi_tip)
        val tvTipValue = view.findViewById<android.widget.TextView>(R.id.tv_bmi_tip_value)
        val btnGotIt = view.findViewById<android.widget.TextView>(R.id.btn_got_it)

        gaugeView.showPointer = false
        gaugeView.updateConfig(state.gender, state.age)
        gaugeView.setBmi(state.bmi, false)

        val rangeAdapter = BmiRangeAdapter()
        rvStatus.layoutManager = LinearLayoutManager(requireContext())
        rvStatus.adapter = rangeAdapter
        rangeAdapter.setData(state.sections, state.bmi)

        val genderStr = if (state.gender == 0) getString(R.string.male) else getString(R.string.female)
        tvTipValue.text = getString(R.string.bmi_teenager_info_tip, state.age.toString(), genderStr)

        btnGotIt.setOnClickListener { dialog.dismiss() }

        if (state.age > 20) {
            tvTip.text = getString(R.string.bmi_adult_tip)
            tvTipValue.visibility = View.GONE
        } else {
            tvTip.text = getString(R.string.bmi_teenager_tip)
            tvTipValue.visibility = View.VISIBLE
        }
        dialog.show()
    }

    private fun saveBmiRecord() {
        val state = viewModel.uiState.value ?: return
        val record = BmiRecord(
            weight = state.weightVal,
            weightUnit = state.weightUnit,
            heightCm = if (state.hUnit == "cm") state.hVal1 else null,
            heightFt = if (state.hUnit == "ft+in") state.hVal1.toInt() else null,
            heightIn = if (state.hUnit == "ft+in") state.hVal2 else null,
            heightUnit = state.hUnit,
            date = state.date,
            timeOfDay = state.time,
            age = state.age,
            gender = if (state.gender == 0) "Male" else "Female",
            bmi = state.bmi
        )
        viewModel.saveRecord(record) {
            val targetTab = if (state.hasDatabaseRecords) 2 else 1
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("SELECT_TAB", targetTab)
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.deleteButton.setOnClickListener {
            val state = viewModel.uiState.value
            viewModel.deleteRecord(state?.recordId ?: -1L) { hasRemaining ->
                dialog.dismiss()
                if (hasRemaining) {
                    // 还有记录，正常回到上一级页面（如历史记录列表）
                    requireActivity().finish()
                } else {
                    // 最后一条记录被删除，进入初始输入页面 (DataInputActivity，填满屏幕)
                    startActivity(Intent(requireContext(), bmicalculator.bmi.calculator.weightlosstracker.ui.activity.DataInputActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                }
            }
        }
        dialog.show()

        dialog.window?.let { window ->
            val marginPx = (37f * resources.displayMetrics.density * 2).toInt()
            val lp = window.attributes
            lp.width = resources.displayMetrics.widthPixels - marginPx
            lp.gravity = Gravity.CENTER
            window.attributes = lp
        }
    }

    override fun onResume() {
        super.onResume()
        // Removed unnecessary redundant call to loadData since we are now observing latestRecord
    }

    override fun onPause() {
        super.onPause()
        if (_binding != null) {
            binding.bmiGauge.onBmiChangeListener = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(data: Bundle? = null): BmiResultFragment {
            return BmiResultFragment().apply {
                arguments = data
            }
        }
    }
}
