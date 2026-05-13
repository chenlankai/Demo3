package bmicalculator.bmi.calculator.weightlosstracker.ui.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import bmicalculator.bmi.calculator.weightlosstracker.util.LocaleUtils

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        // 应用保存的语言设置
        super.attachBaseContext(LocaleUtils.applyLocale(newBase))
    }
}
