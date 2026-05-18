package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.ui.fragment.DataInputFragment

import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity

class DataInputActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_container)

        val container = findViewById<View>(R.id.fragment_container)

        window.navigationBarColor = Color.TRANSPARENT

        // 如果是 Android Q (API 29) 及以上，系统默认会给透明栏强制加一层白色/灰色的防穿帮半透明遮罩
        // 必须把它关闭，底部的 #EAEAEE 颜色才不会被这层遮罩污染变色
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // ✨ 确保容器背景色是你需要的灰色（你也可以直接去 XML 里的 fragment_container 设置 background）
        container.setBackgroundColor(Color.parseColor("#EAEAEE"))

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DataInputFragment())
                .commit()
        }
    }
}
