package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ItemAgeBinding

class AgeAdapter(private val ages: List<Int>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<AgeAdapter.AgeViewHolder>() {

    class AgeViewHolder(val binding: ItemAgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgeViewHolder {
        val binding = ItemAgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AgeViewHolder, position: Int) {
        holder.binding.tvNumber.text = ages[position].toString()
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = ages.size
}
