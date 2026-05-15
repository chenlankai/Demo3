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
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MeActivity : BaseActivity() {

    private lateinit var binding: ActivityMeBinding
    private var isLoggedIn = false

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

        lifecycleScope.launch {
            val bmiDao = AppDatabase.getDatabase(this@MeActivity).bmiDao()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            val fakeRecords = mutableListOf<BmiRecord>()

            val baseWeight = 65f
            var weightOffset = 0f

            // 1. 生成最近 30 天的数据 (每天一条)
            val dayCalendar = Calendar.getInstance()
            for (i in 1..30) {
                dayCalendar.add(Calendar.DAY_OF_YEAR, -1)
                weightOffset += 0.1f
                val currentWeight = baseWeight + weightOffset
                fakeRecords.add(
                    BmiRecord(
                        weight = currentWeight,
                        weightUnit = "kg",
                        heightCm = 175f,
                        heightUnit = "cm",
                        date = dateFormat.format(dayCalendar.time),
                        timeOfDay = if (i % 2 == 0) "Morning" else "Afternoon",
                        age = 25,
                        gender = "Male",
                        bmi = currentWeight / (1.75f * 1.75f),
                        timestamp = dayCalendar.timeInMillis
                    )
                )
            }

            // 2. 生成最近 20 周的数据 (每周一条)
            val weekCalendar = Calendar.getInstance()
            for (i in 1..20) {
                weekCalendar.add(Calendar.WEEK_OF_YEAR, -1)
                weightOffset += 0.4f
                val currentWeight = baseWeight + weightOffset
                fakeRecords.add(
                    BmiRecord(
                        weight = currentWeight,
                        weightUnit = "kg",
                        heightCm = 175f,
                        heightUnit = "cm",
                        date = dateFormat.format(weekCalendar.time),
                        timeOfDay = "Morning",
                        age = 25,
                        gender = "Male",
                        bmi = currentWeight / (1.75f * 1.75f),
                        timestamp = weekCalendar.timeInMillis
                    )
                )
            }

            // 3. 生成最近 2 年的数据 (每月两条)
            val monthCalendar = Calendar.getInstance()
            val totalMonths = 24 
            for (i in 1..totalMonths) {
                monthCalendar.add(Calendar.MONTH, -1)
                weightOffset += 1.5f
                val daysOfMonth = listOf(15, 28)
                for (day in daysOfMonth) {
                    val entryCalendar = monthCalendar.clone() as Calendar
                    entryCalendar.set(Calendar.DAY_OF_MONTH, day)
                    val currentWeight = baseWeight + weightOffset + (if (day == 15) 0.3f else 0f)
                    fakeRecords.add(
                        BmiRecord(
                            weight = currentWeight,
                            weightUnit = "kg",
                            heightCm = 175f,
                            heightUnit = "cm",
                            date = dateFormat.format(entryCalendar.time),
                            timeOfDay = "Morning",
                            age = 25,
                            gender = "Male",
                            bmi = currentWeight / (1.75f * 1.75f),
                            timestamp = entryCalendar.timeInMillis
                        )
                    )
                }
            }

            bmiDao.insertRecords(fakeRecords)
        }

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
                statusMessage = "Logged out successfully"
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

    private fun updateLoginStateUI(dialog: BottomSheetDialog, dialogBinding: ViewGoogleAccountDialogBinding) {
        binding.ivAvatar.isVisible = isLoggedIn
        if (isLoggedIn) {
            dialogBinding.btnLog.text = "Log out"
            dialogBinding.btnLog.setTextColor(Color.parseColor("#F4333C"))
            showStatusToast("Logged in successfully", R.drawable.login, "#4CAF50")
        } else {
            dialogBinding.btnLog.text = "Log in"
            dialogBinding.btnLog.setTextColor(Color.BLACK)
            showStatusToast("Logged out", R.drawable.logout, "#2196F3")
        }
        dialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            binding.ivAvatar.isVisible = isLoggedIn
            if (isLoggedIn) {
                showStatusToast("Logged in successfully", R.drawable.login, "#4CAF50")
            }
        }
    }
}
