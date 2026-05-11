package bmicalculator.bmi.calculator.weightlosstracker.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.FragmentBmiResultBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.MainActivity
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.RecommendAppAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.viewmodel.MainViewModel
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import bmicalculator.bmi.calculator.weightlosstracker.util.CustomTypefaceSpan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class BmiResultFragment : Fragment() {

    private var _binding: FragmentBmiResultBinding? = null
    private val binding get() = _binding!!
    private val rangeAdapter = BmiRangeAdapter()
    private lateinit var recommendAdapter: RecommendAppAdapter

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (binding.layoutToolbar.isVisible) {
                binding.layoutToolbar.updatePadding(top = systemBars.top)
                v.updatePadding(top = 0)
            } else {
                v.updatePadding(top = systemBars.top)
            }
            insets
        }

        val hideDescription = arguments?.getBoolean("hide_description", false) ?: false
        if (hideDescription) {
            binding.tvDescription.visibility = View.GONE
        } else {
            binding.tvDescription.visibility = View.VISIBLE
        }

        setupRecyclerView()
        setupRecommendRecyclerView()
        setupListeners()
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
        binding.tvStatus.setOnClickListener {
            ViewModelProvider(requireActivity())[MainViewModel::class.java].selectTab(0)
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
            startActivity(Intent(requireContext(), bmicalculator.bmi.calculator.weightlosstracker.ui.activity.HistoryActivity::class.java))
        }
    }

    private fun showPassedData(args: Bundle, animate: Boolean = true) {
        // Standalone 模式 UI 控制
        binding.ActionBar1.isVisible = true
        binding.ActionBar2.isVisible = false
        binding.ActionBar3.isVisible = false

        binding.tvSave.isVisible = true
        binding.layoutToolbar.isVisible = true

        lifecycleScope.launch {
            val hasRecords = AppDatabase.getDatabase(requireContext()).bmiDao().getLatestRecord() != null
            val historyBmi = args.getBoolean("history_bmi", false)
            if (historyBmi) {
                binding.ActionBar1.isVisible = false
                binding.ActionBar2.isVisible = false
                binding.ActionBar3.isVisible = true

                binding.rvStatus.isVisible = false
                binding.DivideLine.isVisible = true
                binding.tvNeedApp.isVisible = true
                binding.rvRecommend.isVisible = true
                binding.tvDateTime.isVisible = true
                binding.tvSave.isVisible = false
                
                binding.tvDateTime.text = "${args.getString("EXTRA_DATE")} ${args.getString("EXTRA_TIME")}"
            }
            else if (!hasRecords) {

                Log.d("","数据库无记录")
                binding.rvStatus.isVisible = true
                binding.DivideLine.isVisible = false
                binding.tvNeedApp.isVisible = false
                binding.rvRecommend.isVisible = false
                binding.tvDateTime.isVisible = false
            } else {

                Log.d("","数据库无记录")
                binding.rvStatus.isVisible = false
                binding.DivideLine.isVisible = true
                binding.tvNeedApp.isVisible = true
                binding.rvRecommend.isVisible = true
                binding.tvDateTime.isVisible = false
            }
        }
        binding.tvDescription.isVisible = true

        val bmi = args.getFloat("EXTRA_BMI")
        val gender = args.getInt("EXTRA_GENDER")
        val age = args.getInt("EXTRA_AGE")
        val heightM = args.getFloat("EXTRA_HEIGHT_M")
        val weightVal = args.getFloat("EXTRA_WEIGHT_VAL")
        val weightUnit = args.getString("EXTRA_WEIGHT_UNIT") ?: "kg"
        val hVal1 = args.getFloat("EXTRA_HEIGHT_VAL1")
        val hVal2 = args.getInt("EXTRA_HEIGHT_VAL2")
        val hUnit = args.getString("EXTRA_HEIGHT_UNIT") ?: "cm"

        displayBmiResult(bmi, gender, age, heightM, weightVal, weightUnit, hVal1, hVal2, hUnit, animate)
    }

    private fun loadLatestFromDatabase(animate: Boolean = false) {
        // Tab 模式 UI 控制
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
        
        // 确保 Insets 正确应用
        binding.root.requestApplyInsets()

        lifecycleScope.launch {
            val record = AppDatabase.getDatabase(requireContext()).bmiDao().getLatestRecord()
            if (record != null) {
                binding.scrollView.visibility = View.VISIBLE
                binding.tvToolbarDate.text = record.date


                val heightM = if (record.heightUnit == "cm") {
                    (record.heightCm ?: 0f) / 100f
                } else {
                    val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
                    totalInches * 0.0254f
                }

                val weightKg = if (record.weightUnit == "lb") {
                    record.weight * 0.45359237f
                } else {
                    record.weight
                }
                
                val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f
                
                displayBmiResult(
                    bmi, 
                    if (record.gender == "Male") 0 else 1,
                    record.age,
                    heightM,
                    record.weight,
                    record.weightUnit,
                    if (record.heightUnit == "cm") record.heightCm ?: 0f else (record.heightFt ?: 0).toFloat(),
                    record.heightIn ?: 0,
                    record.heightUnit,
                    animate
                )
            } else {
                // 没有数据时可以隐藏内容或显示提示
                binding.scrollView.visibility = View.GONE
            }
        }
    }

    private fun displayBmiResult(
        bmi: Float, gender: Int, age: Int, heightM: Float,
        weightVal: Float, weightUnit: String, hVal1: Float, hVal2: Int, hUnit: String,
        animate: Boolean = true
    ) {
        binding.bmiGauge.onBmiChangeListener = { animatedValue ->
            binding.tvBmiValue.text = String.format(Locale.US, "%.1f", animatedValue)
        }
        binding.bmiGauge.updateConfig(gender, age)
        binding.bmiGauge.setBmi(bmi, animate)

        val (sections, _) = BmiConfigManager.getConfiguration(gender, age)
        val currentSection = sections.find { bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE) } 
            ?: sections.lastOrNull()

        currentSection?.let {
            binding.tvStatus.text = it.categoryName
            try {
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(it.color))
            } catch (e: Exception) {}
        }

        // 理想体重逻辑
        val normalSection = sections.find { it.categoryName == "Normal" }
        val minIdealBmi = normalSection?.minRange ?: 18.5f
        val maxIdealBmi = normalSection?.maxRange ?: 25.0f
        var minWeight = minIdealBmi * heightM * heightM
        var maxWeight = maxIdealBmi * heightM * heightM
        if (weightUnit == "lb") {
            minWeight /= 0.45359237f
            maxWeight /= 0.45359237f
        }

        val genderStr = if (gender == 0) getString(R.string.male) else getString(R.string.female)
        
        val weightStrFormatted = String.format(Locale.US, if (weightUnit == "lb") "%.2f" else "%.2f", weightVal)
        val hVal1StrFormatted = if (hUnit == "cm") String.format(Locale.US, "%.1f", hVal1) else String.format(Locale.US, "%.0f", hVal1)
        val hVal2StrFormatted = hVal2.toString()

        val heightStrForMsg = if (hUnit == "ft+in") getString(R.string.bmi_height_ft_in_format, hVal1StrFormatted, hVal2StrFormatted) else getString(R.string.bmi_height_cm_format, hVal1StrFormatted)
        binding.tvMessage.text = getString(R.string.bmi_input_data, "$weightStrFormatted $weightUnit", heightStrForMsg, genderStr, age.toString())

        // 描述文本逻辑
        val currentWeight = weightVal
        val shortMessage = getString(R.string.bmi_range_normal_adult_description)
        val isNormal = currentSection?.categoryName.equals("Normal", ignoreCase = true)

        binding.tvDescription.text = if (isNormal) {
            buildSpannedString {
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) { append(shortMessage) }
            }
        } else {
            val rangeStart = getString(R.string.bmi_result_suggest_start, heightStrForMsg)
            val rangeText = getString(R.string.bmi_range_format, minWeight, weightUnit, maxWeight, weightUnit)
            buildSpannedString {
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) { append("$rangeStart ") }
                inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorBlack)) { append(rangeText) }
                when {
                    currentWeight > maxWeight -> {
                        val loseText = getString(R.string.bmi_weight_lose_format, currentWeight - maxWeight, weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) { append(loseText) }
                    }
                    currentWeight < minWeight -> {
                        val gainText = getString(R.string.bmi_weight_gain_format, minWeight - currentWeight, weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) { append(gainText) }
                    }
                }
            }
        }
        rangeAdapter.setData(sections, bmi)
    }

    private fun saveBmiRecord() {
        val args = arguments ?: return
        val weightUnit = args.getString("EXTRA_WEIGHT_UNIT") ?: "kg"
        val heightUnit = args.getString("EXTRA_HEIGHT_UNIT") ?: "cm"
        
        val record = BmiRecord(
            weight = args.getFloat("EXTRA_WEIGHT_VAL"),
            weightUnit = weightUnit,
            heightCm = if (heightUnit == "cm") args.getFloat("EXTRA_HEIGHT_VAL1") else null,
            heightFt = if (heightUnit == "ft+in") args.getFloat("EXTRA_HEIGHT_VAL1").toInt() else null,
            heightIn = if (heightUnit == "ft+in") args.getInt("EXTRA_HEIGHT_VAL2") else null,
            heightUnit = heightUnit,
            date = args.getString("EXTRA_DATE") ?: "",
            timeOfDay = args.getString("EXTRA_TIME") ?: "",
            age = args.getInt("EXTRA_AGE"),
            gender = if (args.getInt("EXTRA_GENDER") == 0) "Male" else "Female"
        )

        lifecycleScope.launch {
            AppDatabase.getDatabase(requireContext()).bmiDao().insertRecord(record)
            requireActivity().getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE).edit().putBoolean("is_first_time", false).apply()
            
            startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("SELECT_TAB", 1) // 保存后跳转到第 2 个 Tab (BMI)
            })
            requireActivity().finish()
        }
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.deleteButton.setOnClickListener {
            val recordId = arguments?.getLong("EXTRA_RECORD_ID", -1L) ?: -1L
            if (recordId != -1L) {
                lifecycleScope.launch {
                    AppDatabase.getDatabase(requireContext()).bmiDao().deleteById(recordId)
                    dialog.dismiss()
                    requireActivity().finish()
                }
            } else {
                dialog.dismiss()
                requireActivity().finish()
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
        
        val args = arguments
        if (args != null && args.containsKey("EXTRA_BMI")) {
            showPassedData(args, true)
        } else {
            loadLatestFromDatabase(false)
        }
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
