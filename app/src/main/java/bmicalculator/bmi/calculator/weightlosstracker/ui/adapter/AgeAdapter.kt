package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ItemAgeBinding

class AgeAdapter(
    private val ages: List<Int>,
    private val onItemSelected: (position: Int) -> Unit
) : RecyclerView.Adapter<AgeAdapter.AgeViewHolder>() {

    private var selectedPosition = 0

    fun setSelectedPosition(pos: Int) {
        if (selectedPosition == pos) return
        val oldPos = selectedPosition
        selectedPosition = pos
        notifyItemChanged(oldPos)
        notifyItemChanged(selectedPosition)
        onItemSelected(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgeViewHolder {
        val binding = ItemAgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AgeViewHolder, position: Int) {
        val age = ages[position]
        val isSelected = position == selectedPosition
        holder.bind(age, isSelected)
    }

    override fun getItemCount() = ages.size

    inner class AgeViewHolder(private val binding: ItemAgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(age: Int, isSelected: Boolean) {
            binding.tvNumber.text = age.toString()
            if (isSelected) {
                binding.tvNumber.setTextColor(android.graphics.Color.parseColor("#3659CF"))
                binding.tvNumber.textSize = 32f
            } else {
                binding.tvNumber.setTextColor(android.graphics.Color.parseColor("#999999"))
                binding.tvNumber.textSize = 32f
            }
        }
    }
}