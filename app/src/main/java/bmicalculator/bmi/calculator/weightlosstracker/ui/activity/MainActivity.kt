package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.MainPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<android.view.View>(R.id.rootLayout)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = bottomInset)
            insets
        }

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        val selectTab = intent.getIntExtra("SELECT_TAB", 0)
        viewPager.setCurrentItem(selectTab, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Calculator"
                    tab.setIcon(R.drawable.icon_bbar_calculator)
                }
                1 -> {
                    tab.text = "BMI"
                    tab.setIcon(R.drawable.icon_bbar_bmi)
                }
                2 -> {
                    tab.text = "Statistics"
                    tab.setIcon(R.drawable.icon_bbar_statistic)
                }

            }
        }.attach()
    }
}
