package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityBmiResultBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.BmiRangeAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*

class BmiResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiResultBinding
    private val rangeAdapter = BmiRangeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBmiResult()
        setupRecyclerView()

        binding.tvDiscard.setOnClickListener {
            showDeleteConfirmDialog()
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

        // 1. 更新仪表盘配置
        binding.bmiGauge.updateConfig(gender, age)
        binding.bmiGauge.setBmi(bmi)

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

        binding.tvBmiValue.text = String.format(Locale.US, "%.1f", bmi)

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
        val genderStr = if (gender == 0) "Male" else "Female"
        val heightStr = if (hUnit == "ft+in") "${hVal1}ft ${hVal2}in " else "$hVal1 cm"
        
        val infoLine1 = "$weightVal $weightUnit | $heightStr | $genderStr | $age years old"
        binding.tvMessage.text = "$infoLine1"

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
