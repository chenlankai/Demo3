package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.data.entity.BmiRecord
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ItemHistoryRecordBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.activity.BmiResultActivity
import bmicalculator.bmi.calculator.weightlosstracker.util.BmiConfigManager
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var records: List<BmiRecord> = emptyList()

    fun setData(newRecords: List<BmiRecord>) {
        this.records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record)
        holder.itemView.setOnClickListener {
            val context = it.context
            
            val heightM = if (record.heightUnit == "cm") {
                (record.heightCm ?: 0f) / 100f
            } else {
                val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
                totalInches * 0.0254f
            }

            BmiResultActivity.start(
                context = context,
                bmi = record.bmi,
                gender = if (record.gender == "Male") 0 else 1,
                age = record.age,
                heightM = heightM,
                date = record.date,
                time = record.timeOfDay,
                weightVal = record.weight,
                weightUnit = record.weightUnit,
                hVal1 = if (record.heightUnit == "cm") record.heightCm ?: 0f else (record.heightFt ?: 0).toFloat(),
                hVal2 = record.heightIn ?: 0,
                hUnit = record.heightUnit,
                isHistory = true,
                recordId = record.id
            )
        }
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(private val binding: ItemHistoryRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: BmiRecord) {
            val bmi = record.bmi
            binding.tvBMI.text = String.format(Locale.US, "%.1f", bmi)

            val genderInt = if (record.gender == "Male") 0 else 1
            val (sections, _) = BmiConfigManager.getConfiguration(genderInt, record.age)
            val currentSection = sections.find { bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE) }
                ?: sections.lastOrNull()

            if (currentSection != null) {
                binding.tvStatus.setText(currentSection.categoryResId)
            } else {
                binding.tvStatus.text = ""
            }
            currentSection?.let {
                try {
                    binding.ivOval.setColorFilter(Color.parseColor(it.color))
                } catch (e: Exception) {
                    binding.ivOval.clearColorFilter()
                }
            }
            
            val dateTimeStr = "${record.date}\n${record.timeOfDay}"
            binding.tvDateTime.text = dateTimeStr
        }
    }
}
