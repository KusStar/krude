package com.kuss.krude.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kuss.krude.R
import com.kuss.krude.data.AppInfo

class AppListAdapter(
    values: List<AppInfo>,
    private val listener: OnItemClickListener?
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
    var apps = values
    var showLabel = true

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
        val item = apps[position]
        holder.apply {
            if (showLabel) {
                labelContainer.visibility = View.VISIBLE
                labelView.text = item.label
                packageNameView.text = item.packageName
            } else {
                labelContainer.visibility = View.GONE
            }

            iconView.setImageBitmap(item.icon)
            container.setOnClickListener {
                item.priority += 1
                listener?.onClick(it, item.packageName)
            }
            container.setOnLongClickListener {
                listener?.onLongClick(item)
                true
            }

        }

    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.container)
        val labelContainer: LinearLayout = view.findViewById(R.id.label_container)
        val labelView: TextView = view.findViewById(R.id.label)
        val packageNameView: TextView = view.findViewById(R.id.package_name)
        val iconView: ImageView = view.findViewById(R.id.icon)
    }

}