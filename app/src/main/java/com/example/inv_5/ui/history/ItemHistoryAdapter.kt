package com.example.inv_5.ui.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.models.ProductTransaction
import java.text.SimpleDateFormat
import java.util.Locale

class ItemHistoryAdapter(
    private var transactions: List<ProductTransaction>,
    private val onItemClick: (ProductTransaction) -> Unit
) : RecyclerView.Adapter<ItemHistoryAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val documentNumberTextView: TextView = view.findViewById(R.id.documentNumberTextView)
        val transactionTypeTextView: TextView = view.findViewById(R.id.transactionTypeTextView)
        val quantityTextView: TextView = view.findViewById(R.id.quantityTextView)
        val rateTextView: TextView = view.findViewById(R.id.rateTextView)
        val balanceTextView: TextView = view.findViewById(R.id.balanceTextView)
        val partnerTextView: TextView = view.findViewById(R.id.partnerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_history, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Format date
        holder.dateTextView.text = dateFormat.format(transaction.date)

        // Document number
        holder.documentNumberTextView.text = transaction.documentNumber

        // Transaction type with color
        when (transaction.documentType) {
            ProductTransaction.TransactionType.PURCHASE -> {
                holder.transactionTypeTextView.text = "PURCHASE"
                holder.transactionTypeTextView.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                holder.quantityTextView.text = "+${transaction.quantity}"
                holder.quantityTextView.setTextColor(Color.parseColor("#4CAF50"))
            }
            ProductTransaction.TransactionType.SALE -> {
                holder.transactionTypeTextView.text = "SALE"
                holder.transactionTypeTextView.setBackgroundColor(Color.parseColor("#F44336")) // Red
                holder.quantityTextView.text = "-${transaction.quantity}"
                holder.quantityTextView.setTextColor(Color.parseColor("#F44336"))
            }
        }

        // Rate
        holder.rateTextView.text = String.format(Locale.getDefault(), "₹%.2f", transaction.rate)

        // Running balance
        holder.balanceTextView.text = transaction.runningBalance.toString()

        // Customer/Supplier
        if (!transaction.customerOrSupplier.isNullOrEmpty()) {
            val prefix = if (transaction.documentType == ProductTransaction.TransactionType.PURCHASE) {
                "Supplier: "
            } else {
                "Customer: "
            }
            holder.partnerTextView.text = prefix + transaction.customerOrSupplier
            holder.partnerTextView.visibility = View.VISIBLE
        } else {
            holder.partnerTextView.visibility = View.GONE
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<ProductTransaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
