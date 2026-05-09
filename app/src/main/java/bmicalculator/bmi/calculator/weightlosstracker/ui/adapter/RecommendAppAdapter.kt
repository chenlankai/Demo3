package bmicalculator.bmi.calculator.weightlosstracker.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bmicalculator.bmi.calculator.weightlosstracker.R

class RecommendAppAdapter(private val dataList: List<RecommendAppModel>) :
    RecyclerView.Adapter<RecommendAppAdapter.ViewHolder>() {

    data class RecommendAppModel(
        val name: String,
        val desc: String,
        val iconRes: Int,
        val rating: String,
        val pkgName: String
    )

    override fun getItemCount(): Int {
        return if (dataList.size > 3) 3 else dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvAppName.text = item.name
        holder.tvAppDesc.text = item.desc
        holder.ivAppIcon.setImageResource(item.iconRes)
        holder.tvRating.text = item.rating

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppName: TextView = view.findViewById(R.id.tvAppName)
        val tvAppDesc: TextView = view.findViewById(R.id.tvAppDesc)
        val ivAppIcon: ImageView = view.findViewById(R.id.ivAppIcon)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
    }
}
