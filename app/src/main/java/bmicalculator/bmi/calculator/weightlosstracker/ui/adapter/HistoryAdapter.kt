package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
            val intent = Intent(context, BmiResultActivity::class.java)
            
            val heightM = if (record.heightUnit == "cm") {
                (record.heightCm ?: 0f) / 100f
            } else {
                val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
                totalInches * 0.0254f
            }

            val weightKg = if (record.weightUnit == "lb") {
                record.weight * 0.45359237f
            } else {
                record.weight
            }

            val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f

            val bundle = Bundle().apply {
                putFloat("EXTRA_BMI", bmi)
                putInt("EXTRA_GENDER", if (record.gender == "Male") 0 else 1)
                putInt("EXTRA_AGE", record.age)
                putFloat("EXTRA_HEIGHT_M", heightM)
                putFloat("EXTRA_WEIGHT_VAL", record.weight)
                putString("EXTRA_WEIGHT_UNIT", record.weightUnit)
                putFloat("EXTRA_HEIGHT_VAL1", if (record.heightUnit == "cm") record.heightCm ?: 0f else (record.heightFt ?: 0).toFloat())
                putInt("EXTRA_HEIGHT_VAL2", record.heightIn ?: 0)
                putString("EXTRA_HEIGHT_UNIT", record.heightUnit)
                putString("EXTRA_DATE", record.date)
                putString("EXTRA_TIME", record.timeOfDay)
                putBoolean("history_bmi", true)
                putLong("EXTRA_RECORD_ID", record.id)
            }
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(private val binding: ItemHistoryRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: BmiRecord) {
            val heightM = if (record.heightUnit == "cm") {
                (record.heightCm ?: 0f) / 100f
            } else {
                val totalInches = (record.heightFt ?: 0) * 12 + (record.heightIn ?: 0)
                totalInches * 0.0254f
            }

            val weightKg = if (record.weightUnit == "lb") {
                record.weight * 0.45359237f
            } else {
                record.weight
            }

            val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f
            binding.tvBMI.text = String.format(Locale.US, "%.1f", bmi)

            val genderInt = if (record.gender == "Male") 0 else 1
            val (sections, _) = BmiConfigManager.getConfiguration(genderInt, record.age)
            val currentSection = sections.find { bmi >= (it.minRange ?: Float.MIN_VALUE) && bmi < (it.maxRange ?: Float.MAX_VALUE) }
                ?: sections.lastOrNull()

            binding.tvStatus.text = currentSection?.categoryName ?: ""
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
