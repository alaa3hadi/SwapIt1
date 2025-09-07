package com.example.swapit1.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R
import com.example.swapit1.databinding.ItemCardBinding
import com.example.swapit1.model.CardItem
import com.example.swapit1.ui.details.Offer_Details_general

class SimpleStringAdapter(private var activity : Activity, private val itemList: List<CardItem>) :
    RecyclerView.Adapter<SimpleStringAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        with(holder.binding) {
            if (item.images.isNotEmpty()) {
                val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.itemImage.setImageBitmap(bitmap)
            } else {
                holder.binding.itemImage.setImageResource(R.drawable.flour10) // صورة افتراضية
            }
            itemTitle.text = item.productName
            itemExchange.text = "للبدل على: ${item.requestedProduct}"
            itemLocation.text = item.location
        }
        holder.binding.root.setOnClickListener {
            val intent = Intent(activity, Offer_Details_general::class.java)
            activity.startActivity(intent)
        }

    }



    override fun getItemCount() = itemList.size
}
private fun decodeBase64ToBitmapSafe(b64: String?): Bitmap? {
    if (b64.isNullOrBlank()) return null
    return try {
        val payload = if (b64.startsWith("data:", true)) b64.substringAfter(',') else b64
        val clean = payload.replace("\\s".toRegex(), "")
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) { null }}