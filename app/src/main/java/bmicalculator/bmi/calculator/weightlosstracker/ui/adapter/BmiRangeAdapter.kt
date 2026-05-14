package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ItemBmiRangeBinding
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import java.util.*

class BmiRangeAdapter : RecyclerView.Adapter<BmiRangeAdapter.ViewHolder>() {

    private var items: List<BmiConfigManager.BmiSection> = emptyList()
    private var currentBmi: Float = 0f

    fun setData(newItems: List<BmiConfigManager.BmiSection>, bmi: Float) {
        this.items = newItems
        this.currentBmi = bmi
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBmiRangeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], currentBmi)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemBmiRangeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: BmiConfigManager.BmiSection, currentBmi: Float) {

            val context = itemView.context

            binding.tvLabel.text = context.getString(section.categoryResId)

            val rangeText = when {
                section.minRange == null -> "< ${String.format(Locale.US, "%.1f", section.maxRange)}"
                section.maxRange == null -> "≥ ${String.format(Locale.US, "%.1f", section.minRange)}"
                else -> String.format(Locale.US, "%.1f- %.1f", section.minRange, section.maxRange - 0.1f)
            }
            binding.tvRange.text = rangeText

            val color = Color.parseColor(section.color)
            binding.viewDot.backgroundTintList = ColorStateList.valueOf(color)

            val isCurrent = when {
                section.minRange == null -> currentBmi < (section.maxRange ?: Float.MAX_VALUE)
                section.maxRange == null -> currentBmi >= (section.minRange ?: Float.MIN_VALUE)
                else -> currentBmi >= section.minRange!! && currentBmi < section.maxRange!!
            }

            if (isCurrent) {
                binding.rootView.setBackgroundResource(R.drawable.bg_bmi_status)
                binding.rootView.backgroundTintList = ColorStateList.valueOf(color)

                binding.viewDot.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                binding.tvLabel.setTextColor(Color.WHITE)
                binding.tvRange.setTextColor(Color.WHITE)
                binding.tvLabel.textSize = 14f
                binding.tvRange.textSize = 14f
                binding.tvLabel.alpha = 1f
                binding.tvRange.alpha = 1f

                val boldTypeface = ResourcesCompat.getFont(context, R.font.montserrat_extrabold)
                binding.tvLabel.typeface = boldTypeface
                binding.tvRange.typeface = boldTypeface
            } else {
                binding.rootView.background = null
                binding.rootView.backgroundTintList = null

                binding.viewDot.backgroundTintList = ColorStateList.valueOf(color)
                binding.tvLabel.setTextColor(Color.BLACK)
                binding.tvRange.setTextColor(Color.BLACK)
                binding.tvLabel.textSize = 14f
                binding.tvRange.textSize = 14f
                binding.tvLabel.alpha = 0.7f
                binding.tvRange.alpha = 0.7f

                val regularTypeface = ResourcesCompat.getFont(context, R.font.montserrat_regular)
                binding.tvLabel.typeface = regularTypeface
                binding.tvRange.typeface = regularTypeface
            }
        }
    }
}
