package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityLanguageBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.LanguageAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import bmicalculator.bmi.calculator.weightlosstracker.util.LocaleUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val languages = listOf(
        getString(R.string.english) to "en",
        getString(R.string.portugu_s) to "pt",
        getString(R.string.languaes_ru) to "ru",
        getString(R.string.languaes_de) to "de",
        getString(R.string.languaes_tw) to "zh-TW",
        getString(R.string.languaes_cn) to "zh-CN",
        getString(R.string.languaes_fr) to "fr",
        getString(R.string.languaes_es) to "es",
        getString(R.string.languaes_it) to "it",
        getString(R.string.languaes_ko) to "ko",
    )
    private var currentLangCode = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // 1. 设置状态栏样式：这里设为全透明（Color.TRANSPARENT）
            // SystemBarStyle.light(亮色模式，意味着图标会自动变成深色)
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),

            // 2. 【核心修改】设置底部导航栏样式：直接指定背景色为 #EAEAEE
            // SystemBarStyle.light 会自动检测并强制把底部的三键客图标变成【深色/黑色】
            navigationBarStyle = SystemBarStyle.light(
                Color.parseColor("#EAEAEE"),
                Color.parseColor("#EAEAEE")
            )
        )
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 从存储中获取当前语言，若无则取系统当前语言
        currentLangCode = LocaleUtils.getLanguage(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutToolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.endLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom + (15 * resources.displayMetrics.density).toInt()
            v.layoutParams = params

            insets
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.rvLanguage.adapter = LanguageAdapter(languages, currentLangCode) { selectedCode ->
            if (currentLangCode != selectedCode) {
                LocaleUtils.setLocale(this, selectedCode)
                
                lifecycleScope.launch {
                    val records = AppDatabase.getDatabase(this@LanguageActivity).bmiDao().getAllRecords().first()
                    
                    val targetActivity = if (records.isEmpty()) {
                        DataInputActivity::class.java
                    } else {
                        MainActivity::class.java
                    }
                    
                    val intent = Intent(this@LanguageActivity, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                finish()
            }
        }
    }
}
