package com.example.myownlauncher.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myownlauncher.R
import com.example.myownlauncher.data.AppInfo

class AppListAdapter(
    private val showIcons: Boolean = false,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)

        fun bind(app: AppInfo) {
            // Set app name
            appName.text = app.getDisplayName()

            // Show/hide icon
            appIcon.visibility = if (showIcons) View.VISIBLE else View.GONE
            if (showIcons) {
                appIcon.setImageDrawable(app.icon)
            }

            // Apply custom background color
            if (app.backgroundColor != null) {
                cardView.setCardBackgroundColor(app.backgroundColor!!)
            } else {
                cardView.setCardBackgroundColor(Color.TRANSPARENT)
            }

            // Apply custom text color
            if (app.textColor != null) {
                appName.setTextColor(app.textColor!!)
            } else {
                // Default text color (from theme)
                appName.setTextColor(
                    itemView.context.getColor(android.R.color.primary_text_dark)
                )
            }

            // Click listeners
            itemView.setOnClickListener {
                onAppClick(app)
            }

            itemView.setOnLongClickListener {
                onAppLongClick(app)
                true
            }
        }
    }

    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}