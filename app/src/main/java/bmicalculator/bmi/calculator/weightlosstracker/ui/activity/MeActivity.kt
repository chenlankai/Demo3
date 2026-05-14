package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
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

import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogSyncIssueBinding
import androidx.appcompat.app.AlertDialog
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity

class MeActivity : BaseActivity() {

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
        binding.ivRefresh.setOnClickListener { 
            showSyncIssueDialog()
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

        binding.btnLanguage.setOnClickListener {
            android.content.Intent(this, LanguageActivity::class.java).also {
                startActivity(it)
            }
        }
        
    }

    private fun showSyncIssueDialog() {
        val dialogBinding = DialogSyncIssueBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.btnDone.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showGoogleAccountDialog() {
        val dialogBinding = ViewGoogleAccountDialogBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this, R.style.TransparentBottomSheetDialogTheme)
        dialog.setContentView(dialogBinding.root)

        // 1. 初始化对话框 UI 状态
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

            val statusMessage: String
            val iconRes: Int
            val colorStr: String

            if (isLoggedIn) {
                statusMessage = "Logged in successfully"
                iconRes = R.drawable.login
                colorStr = "#4CAF50"
            } else {
                statusMessage = "Logged out"
                iconRes = R.drawable.logout
                colorStr = "#2196F3"
            }

            showStatusToast(statusMessage, iconRes, colorStr)

            dialog.dismiss()
        }

        dialog.behavior.isDraggable = false
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }
}
