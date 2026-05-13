package bmicalculator.bmi.calculator.weightlosstracker

import android.app.Application
import android.content.Context
import bmicalculator.bmi.calculator.weightlosstracker.util.LocaleUtils

class BmiApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.applyLocale(base))
    }
}
