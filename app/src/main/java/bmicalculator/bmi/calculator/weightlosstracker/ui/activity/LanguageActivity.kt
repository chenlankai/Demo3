package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityLanguageBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.LanguageAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import bmicalculator.bmi.calculator.weightlosstracker.util.LocaleUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageBinding
    /*private val languages = listOf(
        "English" to "en",
        "中文" to "zh",
        "Español" to "es",
        "Français" to "fr",
        "Deutsch" to "de",
        "日本語" to "ja",
        "한국어" to "ko",
        "Português" to "pt",
        "Русский" to "ru",
        "العربية" to "ar",
        "Italiano" to "it",
        "Türkçe" to "tr",
        "Tiếng Việt" to "vi",
        "ภาษาไทย" to "th",
        "Polski" to "pl",
        "Nederlands" to "nl",
        "Bahasa Indonesia" to "in",
        "فارسی" to "fa"
    )*/
    private val languages = listOf(
        "English" to "en",
        "Português" to "pt",
        "Русский" to "ru",
        //"Português" to "pt",
        "Deutsch" to "de",
        "繁體中文" to "zh-TW",
        "简体中文" to "zh-CN",
        "Français" to "fr",
        "Español" to "es",
        "Italiano" to "it",
        "한국어" to "ko",
    )
    private var currentLangCode = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
