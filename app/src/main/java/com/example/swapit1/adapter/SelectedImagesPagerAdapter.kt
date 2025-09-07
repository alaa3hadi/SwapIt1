package com.example.swapit1.adapter

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R

class SelectedImagesPagerAdapter(
    private val images: List<Any>, // Bitmap أو Uri
    private val onRemoveClick: ((Any) -> Unit)? = null,
    private val onAddClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_IMAGE = 1
    private val TYPE_ADD = 2

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSelected)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnAdd: ImageView = view.findViewById(R.id.btnAddNew)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < images.size) TYPE_IMAGE else TYPE_ADD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_selected_image_pager, parent, false)
            ImageViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_image_pager, parent, false)
            AddViewHolder(v)
        }
    }

    override fun getItemCount(): Int = images.size + if (onAddClick != null) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            val item = images[position]
            when (item) {
                is Uri -> holder.img.setImageURI(item)
                is Bitmap -> holder.img.setImageBitmap(item)
            }

            if (onRemoveClick != null) {
                holder.btnRemove.visibility = View.VISIBLE
                holder.btnRemove.setOnClickListener { onRemoveClick?.let { it1 -> it1(item) } }
            } else {
                holder.btnRemove.visibility = View.GONE
            }

        } else if (holder is AddViewHolder) {
            onAddClick?.let { click ->
                holder.btnAdd.visibility = View.VISIBLE
                holder.btnAdd.setOnClickListener { click() }
            } ?: run {
                holder.btnAdd.visibility = View.GONE
            }
        }
    }
}
