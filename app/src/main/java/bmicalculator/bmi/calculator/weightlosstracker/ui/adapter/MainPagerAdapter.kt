package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import bmicalculator.bmi.calculator.weightlosstracker.ui.fragment.BmiResultFragment
import bmicalculator.bmi.calculator.weightlosstracker.ui.fragment.DataInputFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DataInputFragment()
            1 -> BmiResultFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("hide_description", true)
                }
            }
            2 -> Fragment() // Statistics placeholder
            else -> DataInputFragment()
        }
    }
}