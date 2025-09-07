package com.example.swapit1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.databinding.ItemCategoryBinding
import com.example.swapit1.model.CategoryItem

class CategoryAdapter(
    private val categoryList: List<CategoryItem>,
    private val onItemClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryList[position]
        holder.binding.apply {
            categoryName.text = item.name
            categoryImage.setImageResource(item.imageResId)
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = categoryList.size
}
