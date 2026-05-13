package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityLanguageBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.LanguageAdapter

class LanguageActivity : AppCompatActivity() {

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutToolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.rvLanguage.adapter = LanguageAdapter(languages, currentLangCode) { selectedCode ->
            currentLangCode = selectedCode
            // 这里通常会添加切换语言的逻辑，并刷新页面
            finish() 
        }
    }
}
