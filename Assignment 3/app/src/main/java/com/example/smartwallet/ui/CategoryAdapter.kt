package com.example.smartwallet.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.example.smartwallet.R
import com.example.smartwallet.ui.AddTransactionFragment.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = -1

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCategoryIcon: MaterialCardView = itemView.findViewById(R.id.card_category_icon)
        private val tvCategoryIcon: TextView = itemView.findViewById(R.id.tv_category_icon)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)

        fun bind(category: Category, position: Int) {
            tvCategoryIcon.text = category.icon
            tvCategoryName.text = category.name

            if (position == selectedPosition) {
                cardCategoryIcon.strokeColor = Color.parseColor("#3498DB")
                cardCategoryIcon.strokeWidth = 8
                cardCategoryIcon.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
            } else {
                cardCategoryIcon.strokeColor = Color.parseColor("#E0E0E0")
                cardCategoryIcon.strokeWidth = 4
                cardCategoryIcon.setCardBackgroundColor(Color.WHITE)
            }

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun setSelectedCategory(category: Category) {
        selectedPosition = categories.indexOf(category)
        notifyDataSetChanged()
    }
}