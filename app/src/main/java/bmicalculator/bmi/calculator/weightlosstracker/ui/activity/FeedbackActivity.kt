package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityFeedbackBinding
import kotlin.math.max

class FeedbackActivity : BaseActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // 1. 设置状态栏样式：这里设为全透明（Color.TRANSPARENT）
            // SystemBarStyle.light(亮色模式，意味着图标会自动变成深色)
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),

            // 2. 设置底部导航栏样式：直接指定背景色为 #EAEAEE
            // SystemBarStyle.light 会自动检测并强制把底部的三键客图标变成【深色/黑色】
            navigationBarStyle = SystemBarStyle.light(
                Color.parseColor("#EAEAEE"),
                Color.parseColor("#EAEAEE")
            )
        )
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            binding.layoutToolbar.updatePadding(top = systemBars.top)

            v.updatePadding(bottom = max(systemBars.bottom, ime.bottom))
            
            insets
        }

        binding.btnSave.isEnabled = false
        binding.etFeedback.doOnTextChanged { text, _, _, _ ->
            binding.btnSave.isEnabled = !text.isNullOrBlank()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            if (binding.btnSave.isEnabled) {
                val userFeedback = binding.etFeedback.text.toString().trim()


                val fullMessage = getString(R.string.toast_feedback_text, userFeedback)

                requestToastOnBack(
                    fullMessage,
                    R.drawable.check_circle,
                    "#32CD32"
                )
                finish()
            }
        }
    }
}
