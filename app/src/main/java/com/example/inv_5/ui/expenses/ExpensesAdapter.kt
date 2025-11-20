package com.example.inv_5.ui.expenses

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.model.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpensesAdapter(
    private var expenses: List<Expense>,
    private val onExpenseClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExpenseDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        private val tvExpenseType: TextView = itemView.findViewById(R.id.tvExpenseType)
        private val tvExpenseCategory: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val tvExpenseDescription: TextView = itemView.findViewById(R.id.tvExpenseDescription)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)

        fun bind(expense: Expense) {
            tvExpenseDate.text = dateFormat.format(expense.expenseDate)
            tvExpenseCategory.text = expense.expenseCategory
            tvTotalAmount.text = currencyFormat.format(expense.totalAmount)
            tvExpenseDescription.text = expense.description
            tvExpenseType.text = expense.expenseType

            // Set badge color based on expense type
            when (expense.expenseType) {
                "CAPEX" -> {
                    tvExpenseType.setBackgroundColor(Color.parseColor("#FF6F00")) // Orange
                }
                "OPEX" -> {
                    tvExpenseType.setBackgroundColor(Color.parseColor("#0D47A1")) // Navy Blue
                }
                "MIXED" -> {
                    tvExpenseType.setBackgroundColor(Color.parseColor("#7B1FA2")) // Purple
                }
                else -> {
                    tvExpenseType.setBackgroundColor(Color.parseColor("#616161")) // Gray
                }
            }

            // Show payment status badge if available
            if (expense.paymentStatus.isNotEmpty()) {
                tvPaymentStatus.visibility = View.VISIBLE
                tvPaymentStatus.text = expense.paymentStatus
                
                when (expense.paymentStatus) {
                    "Paid" -> tvPaymentStatus.setBackgroundColor(Color.parseColor("#43A047")) // Green
                    "Pending" -> tvPaymentStatus.setBackgroundColor(Color.parseColor("#FF6F00")) // Orange
                    "Partial" -> tvPaymentStatus.setBackgroundColor(Color.parseColor("#FFA726")) // Light Orange
                    else -> tvPaymentStatus.setBackgroundColor(Color.parseColor("#616161")) // Gray
                }
            } else {
                tvPaymentStatus.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onExpenseClick(expense)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_card, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount() = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
