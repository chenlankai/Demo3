package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityBmiResultBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.util.CustomTypefaceSpan

import java.util.Locale


class BmiResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiResultBinding
    private val rangeAdapter = BmiRangeAdapter()

    private val regularTypeface by lazy { ResourcesCompat.getFont(this, R.font.montserrat_regular) }
    private val extraBoldTypeface by lazy { ResourcesCompat.getFont(this, R.font.montserrat_extrabold) }
    private val colorBlack = Color.BLACK
    private val colorRed = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBmiResult()
        setupRecyclerView()

        binding.tvDiscard.setOnClickListener {
            showDeleteConfirmDialog()
        }

        binding.tvSave.setOnClickListener {
            saveBmiRecord()
        }
    }

    private fun saveBmiRecord() {
        val gender = intent.getIntExtra("EXTRA_GENDER", 0)
        val age = intent.getIntExtra("EXTRA_AGE", 25)
        val date = intent.getStringExtra("EXTRA_DATE") ?: ""
        val time = intent.getStringExtra("EXTRA_TIME") ?: ""
        val weightVal = intent.getStringExtra("EXTRA_WEIGHT_VAL") ?: ""
        val weightUnit = intent.getStringExtra("EXTRA_WEIGHT_UNIT") ?: "kg"
        val hVal1 = intent.getStringExtra("EXTRA_HEIGHT_VAL1") ?: ""
        val hVal2 = intent.getStringExtra("EXTRA_HEIGHT_VAL2") ?: ""
        val hUnit = intent.getStringExtra("EXTRA_HEIGHT_UNIT") ?: "cm"

        val record = BmiRecord(
            weight = weightVal.toFloatOrNull() ?: 0f,
            weightUnit = weightUnit,
            heightCm = if (hUnit == "cm") hVal1.toFloatOrNull() else null,
            heightFt = if (hUnit == "ft+in") hVal1.toIntOrNull() else null,
            heightIn = if (hUnit == "ft+in") hVal2.toIntOrNull() else null,
            heightUnit = hUnit,
            date = date,
            timeOfDay = time,
            age = age,
            gender = if (gender == 0) "Male" else "Female"
        )

        lifecycleScope.launch {
            AppDatabase.getDatabase(this@BmiResultActivity).bmiDao().insertRecord(record)

            val prefs = getSharedPreferences("bmi_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("is_first_time", false).apply()

            val intent = Intent(this@BmiResultActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBmiResult() {
        val bmi = intent.getFloatExtra("EXTRA_BMI", 22f)
        val gender = intent.getIntExtra("EXTRA_GENDER", 0)
        val age = intent.getIntExtra("EXTRA_AGE", 25)
        val heightM = intent.getFloatExtra("EXTRA_HEIGHT_M", 1.7f)
        
        val date = intent.getStringExtra("EXTRA_DATE") ?: ""
        val time = intent.getStringExtra("EXTRA_TIME") ?: ""
        val weightVal = intent.getStringExtra("EXTRA_WEIGHT_VAL") ?: ""
        val weightUnit = intent.getStringExtra("EXTRA_WEIGHT_UNIT") ?: "kg"
        val hVal1 = intent.getStringExtra("EXTRA_HEIGHT_VAL1") ?: ""
        val hVal2 = intent.getStringExtra("EXTRA_HEIGHT_VAL2") ?: ""
        val hUnit = intent.getStringExtra("EXTRA_HEIGHT_UNIT") ?: "cm"
        // 绑定监听器：当 View 内部动画播放时，这里会不断被调用
        binding.bmiGauge.onBmiChangeListener = { animatedValue ->
            // 更新显示 BMI 数字的 TextView
            binding.tvBmiValue.text = String.format(Locale.US, "%.1f", animatedValue)
        }

        // 更新配置并启动动画
        binding.bmiGauge.updateConfig(gender, age)
        binding.bmiGauge.setBmi(bmi) // 调用此方法后，动画开始，上面的监听器生效

        // 2. 获取当前 BMI 所在的分类
        val (sections, _) = BmiConfigManager.getConfiguration(gender, age)
        var currentSection: BmiConfigManager.BmiSection? = null
        
        for (section in sections) {
            val min = section.minRange ?: Float.MIN_VALUE
            val max = section.maxRange ?: Float.MAX_VALUE
            if (bmi >= min && bmi < max) {
                currentSection = section
                break
            }
        }
        if (currentSection == null && sections.isNotEmpty()) {
            currentSection = sections.last()
        }

        // 3. 设置状态文本和背景色
        currentSection?.let {
            binding.tvStatus.text = it.categoryName
            try {
                val color = Color.parseColor(it.color)
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
            } catch (e: Exception) {
                binding.tvStatus.setTextColor(Color.BLACK)
            }
        }

        // 4. 计算理想体重范围 (基于 Normal BMI 18.5 - 25.0)
        val normalSection = sections.find { it.categoryName == "Normal" }
        val minIdealBmi = normalSection?.minRange ?: 18.5f
        val maxIdealBmi = normalSection?.maxRange ?: 25.0f
        
        var minWeight = minIdealBmi * heightM * heightM
        var maxWeight = maxIdealBmi * heightM * heightM
        
        // 如果用户使用的是 lb，将理想体重也转换为 lb 显示
        if (weightUnit == "lb") {
            minWeight /= 0.45359237f
            maxWeight /= 0.45359237f
        }

        // 5. 拼接显示详细信息
        val genderStr = if (gender == 0) getString(R.string.male) else getString(R.string.female)
        val heightStr = if (hUnit == "ft+in") {
            getString(R.string.bmi_height_ft_in_format, hVal1, hVal2)
        } else {
            getString(R.string.bmi_height_cm_format, hVal1)
        }
        
        val infoLine1 = getString(R.string.bmi_input_data, "$weightVal $weightUnit", heightStr, genderStr, age.toString())
        binding.tvMessage.text = infoLine1

        val currentWeight = weightVal.toFloatOrNull() ?: 0f
        val shortMessage = getString(R.string.bmi_range_normal_adult_description)
        val isNormal = currentSection?.categoryName.equals("Normal", ignoreCase = true)
        binding.tvDescription.background
        binding.tvDescription.text = if (isNormal) {
            // 情况 A: 正常状态 - 黑色 + Regular
            buildSpannedString {
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) {
                    append(shortMessage)
                }
            }
        } else {
            // 情况 B: 非正常状态 - 复杂混合样式
            val rangeStart = getString(R.string.bmi_result_suggest_start, heightStr)
            val rangeText = getString(R.string.bmi_range_format, minWeight, weightUnit, maxWeight, weightUnit)

            buildSpannedString {
                // 第一部分：rangeStart -> 黑色 + Regular
                inSpans(CustomTypefaceSpan(regularTypeface!!), ForegroundColorSpan(colorBlack)) {
                    append("$rangeStart ")
                }

                // 第二部分：rangeText -> 黑色 + ExtraBold
                inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorBlack)) {
                    append(rangeText)
                }

                // 第三部分：增减建议 -> 红色 + ExtraBold
                when {
                    currentWeight > maxWeight -> {
                        val loseText = getString(R.string.bmi_weight_lose_format, currentWeight - maxWeight, weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) {
                            append(loseText)
                        }
                    }
                    currentWeight < minWeight -> {
                        val gainText = getString(R.string.bmi_weight_gain_format, minWeight - currentWeight, weightUnit)
                        inSpans(CustomTypefaceSpan(extraBoldTypeface!!), ForegroundColorSpan(colorRed)) {
                            append(gainText)
                        }
                    }
                }
            }
        }

        // 6. 更新列表数据
        rangeAdapter.setData(sections, bmi)
    }

    private fun setupRecyclerView() {
        binding.rvStatus.apply {
            layoutManager = LinearLayoutManager(this@BmiResultActivity)
            adapter = rangeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.deleteButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()

        val window = dialog.window
        if (window != null) {
            val density = resources.displayMetrics.density
            val marginPx = (37f * density * 2).toInt()
            val screenWidth = resources.displayMetrics.widthPixels

            val lp = window.attributes
            lp.width = screenWidth - marginPx
            lp.gravity = Gravity.CENTER
            window.attributes = lp
        }
    }
}
