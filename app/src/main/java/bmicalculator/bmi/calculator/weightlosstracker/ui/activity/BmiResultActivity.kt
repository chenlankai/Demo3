package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.ui.fragment.BmiResultFragment

import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity

class BmiResultActivity : BaseActivity() {

    companion object {
        const val EXTRA_BMI = "EXTRA_BMI"
        const val EXTRA_GENDER = "EXTRA_GENDER"
        const val EXTRA_AGE = "EXTRA_AGE"
        const val EXTRA_HEIGHT_M = "EXTRA_HEIGHT_M"
        const val EXTRA_DATE = "EXTRA_DATE"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_WEIGHT_VAL = "EXTRA_WEIGHT_VAL"
        const val EXTRA_WEIGHT_UNIT = "EXTRA_WEIGHT_UNIT"
        const val EXTRA_HEIGHT_VAL1 = "EXTRA_HEIGHT_VAL1"
        const val EXTRA_HEIGHT_VAL2 = "EXTRA_HEIGHT_VAL2"
        const val EXTRA_HEIGHT_UNIT = "EXTRA_HEIGHT_UNIT"
        const val EXTRA_HISTORY_BMI = "history_bmi"
        const val EXTRA_RECORD_ID = "EXTRA_RECORD_ID"

        fun start(
            context: Context,
            bmi: Float,
            gender: Int,
            age: Int,
            heightM: Float,
            date: String?,
            time: String?,
            weightVal: Float,
            weightUnit: String?,
            hVal1: Float,
            hVal2: Int,
            hUnit: String?,
            isHistory: Boolean = false,
            recordId: Long = -1L
        ) {
            val intent = Intent(context, BmiResultActivity::class.java).apply {
                putExtra(EXTRA_BMI, bmi)
                putExtra(EXTRA_GENDER, gender)
                putExtra(EXTRA_AGE, age)
                putExtra(EXTRA_HEIGHT_M, heightM)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_TIME, time)
                putExtra(EXTRA_WEIGHT_VAL, weightVal)
                putExtra(EXTRA_WEIGHT_UNIT, weightUnit)
                putExtra(EXTRA_HEIGHT_VAL1, hVal1)
                putExtra(EXTRA_HEIGHT_VAL2, hVal2)
                putExtra(EXTRA_HEIGHT_UNIT, hUnit)
                putExtra(EXTRA_HISTORY_BMI, isHistory)
                putExtra(EXTRA_RECORD_ID, recordId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_container)

        val container = findViewById<android.view.View>(R.id.fragment_container)
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            // 将 Activity 接收到的 Intent Extras 转发给 Fragment
            val fragment = BmiResultFragment.newInstance(intent.extras)
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
