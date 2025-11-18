package com.example.inv_5.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.models.ActivityType
import com.example.inv_5.data.models.RecentActivity
import com.example.inv_5.databinding.ItemRecentActivityBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentActivityAdapter(
    private var activities: List<RecentActivity> = emptyList(),
    private val onItemClick: (RecentActivity) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemRecentActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size

    fun updateActivities(newActivities: List<RecentActivity>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    inner class ActivityViewHolder(
        private val binding: ItemRecentActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: RecentActivity) {
            binding.apply {
                // Set document number
                activityDocument.text = activity.documentNumber

                // Set customer/supplier
                activityCustomer.text = activity.customerOrSupplier ?: "N/A"

                // Set date
                activityDate.text = dateFormat.format(Date(activity.date))

                // Set item count
                activityItems.text = "${activity.itemCount} item${if (activity.itemCount != 1) "s" else ""}"

                // Set amount
                activityAmount.text = currencyFormat.format(activity.totalAmount)

                // Set icon and background color based on type
                val iconRes: Int
                val bgColor: Int
                when (activity.type) {
                    ActivityType.PURCHASE -> {
                        iconRes = android.R.drawable.ic_input_add
                        bgColor = ContextCompat.getColor(root.context, R.color.green)
                    }
                    ActivityType.SALE -> {
                        iconRes = android.R.drawable.ic_menu_send
                        bgColor = ContextCompat.getColor(root.context, R.color.red_error)
                    }
                }

                activityIcon.setImageResource(iconRes)
                
                // Create circular background
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(bgColor)
                }
                activityIcon.background = drawable

                // Set click listener
                root.setOnClickListener {
                    onItemClick(activity)
                }
            }
        }
    }
}
