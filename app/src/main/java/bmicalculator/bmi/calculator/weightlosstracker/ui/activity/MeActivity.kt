package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.isVisible
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityMeBinding
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ViewGoogleAccountDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class MeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeBinding
    private var isLoggedIn = false // 记录登录状态

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutToolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.cardBackup.setOnClickListener {
            showGoogleAccountDialog()
        }

        binding.btnFeedback.setOnClickListener {
            android.content.Intent(this, FeedbackActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun showGoogleAccountDialog() {
        val dialogBinding = ViewGoogleAccountDialogBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this, R.style.TransparentBottomSheetDialogTheme)
        dialog.setContentView(dialogBinding.root)

        if (isLoggedIn) {
            dialogBinding.btnLog.text = "Log out"
            dialogBinding.btnLog.setTextColor(Color.parseColor("#F4333C"))

        } else {
            dialogBinding.btnLog.text = "Log in"
            dialogBinding.btnLog.setTextColor(Color.BLACK)

        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnLog.setOnClickListener {

            isLoggedIn = !isLoggedIn

            binding.ivAvatar.isVisible = isLoggedIn

            Toast.makeText(this, if (isLoggedIn) "Logged in successfully" else "Logged out", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.behavior.isDraggable = false
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }
}
