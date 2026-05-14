package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
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
        enableEdgeToEdge()
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

                BaseActivity.requestToastOnBack(
                    fullMessage,
                    R.drawable.check_circle,
                    "#32CD32"
                )
                finish()
            }
        }
    }
}
