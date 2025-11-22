package com.example.smartwallet.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartwallet.R
import com.example.smartwallet.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val formatCurrency: (Double) -> String
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryIcon: TextView = itemView.findViewById(R.id.tv_category_icon)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvNote: TextView = itemView.findViewById(R.id.tv_note)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)

        fun bind(transaction: Transaction) {
            tvCategoryIcon.text = transaction.categoryIcon
            tvCategoryName.text = transaction.categoryName
            tvNote.text = transaction.note
            tvDate.text = formatDate(transaction.date)

            if (transaction.type == "INCOME") {
                tvAmount.text = "+${formatCurrency(transaction.amount)}"
                tvAmount.setTextColor(Color.parseColor("#27AE60"))
            } else {
                tvAmount.text = "-${formatCurrency(transaction.amount)}"
                tvAmount.setTextColor(Color.parseColor("#E74C3C"))
            }

            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}