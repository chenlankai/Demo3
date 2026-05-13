package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val list: List<Pair<String, String>>,
    private val currentCode: String,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, code) = list[position]
        holder.binding.tvLanguage.text = name
        holder.binding.ivCheck.isVisible = (code == currentCode)
        
        // 最后一项隐藏分割线
        holder.binding.vDivideLine.isVisible = position != list.size - 1
        
        holder.binding.root.setOnClickListener {
            onItemClick(code)
        }
    }

    override fun getItemCount(): Int = list.size
}
