package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Intent
import android.os.Bundle
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
    private val languages = listOf(
        "English" to "en",
        "Chinese" to "zh",
        "Spanish" to "es",
        "French" to "fr",
        "German" to "de",
        "Japanese" to "ja",
        "Korean" to "ko",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Arabic" to "ar",
        "Italian" to "it",
        "Turkish" to "tr",
        "Vietnamese" to "vi",
        "Thai" to "th",
        "Polish" to "pl",
        "Dutch" to "nl",
        "Indonesian" to "in",
        "Persian" to "fa"
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
