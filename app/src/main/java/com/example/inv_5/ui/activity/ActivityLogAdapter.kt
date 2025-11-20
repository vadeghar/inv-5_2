package com.example.inv_5.ui.activity

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.ActivityLog
import com.example.inv_5.data.repository.ActivityLogRepository
import com.example.inv_5.databinding.ItemActivityLogBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityLogAdapter(
    private var activities: List<ActivityLog> = emptyList(),
    private val onItemClick: (ActivityLog) -> Unit
) : RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLogViewHolder {
        val binding = ItemActivityLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityLogViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size

    fun updateActivities(newActivities: List<ActivityLog>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    inner class ActivityLogViewHolder(
        private val binding: ItemActivityLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityLog) {
            binding.apply {
                // Set description
                tvDescription.text = activity.description

                // Set document number
                tvDocumentNumber.text = activity.documentNumber ?: activity.entityId

                // Set additional info
                tvAdditionalInfo.text = activity.additionalInfo ?: "-"

                // Set date
                tvDate.text = dateFormat.format(activity.timestamp)

                // Set amount
                activity.amount?.let {
                    tvAmount.text = currencyFormat.format(it)
                } ?: run {
                    tvAmount.text = "-"
                }

                // Set activity type badge
                tvActivityType.text = activity.activityType
                tvActivityType.setBackgroundColor(
                    when (activity.activityType) {
                        ActivityLogRepository.ActivityType.ADD -> 
                            ContextCompat.getColor(root.context, R.color.green)
                        ActivityLogRepository.ActivityType.EDIT -> 
                            ContextCompat.getColor(root.context, R.color.orange)
                        ActivityLogRepository.ActivityType.DELETE -> 
                            ContextCompat.getColor(root.context, R.color.red_error)
                        else -> 
                            ContextCompat.getColor(root.context, R.color.grey)
                    }
                )

                // Set icon based on entity type
                val iconRes: Int
                val bgColor: Int
                when (activity.entityType) {
                    ActivityLogRepository.EntityType.PURCHASE -> {
                        iconRes = android.R.drawable.ic_input_add
                        bgColor = ContextCompat.getColor(root.context, R.color.green)
                    }
                    ActivityLogRepository.EntityType.SALE -> {
                        iconRes = android.R.drawable.ic_menu_send
                        bgColor = ContextCompat.getColor(root.context, R.color.red_error)
                    }
                    ActivityLogRepository.EntityType.EXPENSE -> {
                        iconRes = android.R.drawable.ic_delete
                        bgColor = ContextCompat.getColor(root.context, R.color.orange)
                    }
                    ActivityLogRepository.EntityType.PRODUCT -> {
                        iconRes = android.R.drawable.ic_menu_edit
                        bgColor = ContextCompat.getColor(root.context, R.color.primary_navy_blue)
                    }
                    ActivityLogRepository.EntityType.CUSTOMER -> {
                        iconRes = android.R.drawable.ic_menu_myplaces
                        bgColor = ContextCompat.getColor(root.context, R.color.purple)
                    }
                    ActivityLogRepository.EntityType.SUPPLIER -> {
                        iconRes = android.R.drawable.ic_menu_myplaces
                        bgColor = ContextCompat.getColor(root.context, R.color.teal)
                    }
                    ActivityLogRepository.EntityType.REPORT -> {
                        iconRes = android.R.drawable.ic_menu_view
                        bgColor = ContextCompat.getColor(root.context, R.color.light_green)
                    }
                    else -> {
                        iconRes = android.R.drawable.ic_menu_info_details
                        bgColor = ContextCompat.getColor(root.context, R.color.grey)
                    }
                }

                ivIcon.setImageResource(iconRes)
                
                // Create circular background
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(bgColor)
                }
                ivIcon.background = drawable

                // Set click listener
                root.setOnClickListener {
                    onItemClick(activity)
                }
            }
        }
    }
}
