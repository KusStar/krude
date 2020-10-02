package com.kuss.krude.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kuss.krude.R
import com.kuss.krude.models.AppInfo

class AppListAdapter(
    private val values: List<AppInfo>,
    private val listener: OnItemClickListener?
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(view: View, packageName: String)

        fun onLongClick(item: AppInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.labelView.text = item.label
        holder.packageNameView.text = item.packageName
        holder.iconView.setImageDrawable(item.icon)
        holder.container.setOnClickListener {
            listener?.onClick(it, item.packageName)
        }
        holder.container.setOnLongClickListener{
            listener?.onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.container)
        val labelView: TextView = view.findViewById(R.id.label)
        val packageNameView: TextView = view.findViewById(R.id.package_name)
        val iconView: ImageView = view.findViewById(R.id.icon)
    }

}