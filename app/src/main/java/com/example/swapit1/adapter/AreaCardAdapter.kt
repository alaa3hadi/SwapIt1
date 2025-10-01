package com.example.swapit1.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R
import com.example.swapit1.databinding.ItemAreaCardBinding
import com.example.swapit1.model.AreaItem
import com.example.swapit1.ui.details.Offer_Details_general
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AreaCardAdapter(
    private val requesterId: String ,
    private val requesterName: String,
    private val activity: Activity,
    private val itemList: MutableList<AreaItem> = mutableListOf()
) : RecyclerView.Adapter<AreaCardAdapter.AreaCardViewHolder>() {
    fun submit(newItems: List<AreaItem>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class AreaCardViewHolder(val binding: ItemAreaCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AreaCardViewHolder(
            ItemAreaCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: AreaCardViewHolder, position: Int) {
        val item = itemList[position]
        with(holder.binding) {
            if (item.images.isNotEmpty()) {
                val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.areaImage.setImageBitmap(bitmap)
            } else {
                holder.binding.areaImage.setImageResource(R.drawable.ic_image_placeholder) // صورة افتراضية
            }


            // else itemImage.setImageResource(item.imageResId)
            val timestampMillis = item.createdAt?.toDate()?.time ?: 0L
            itemTime.text = if (timestampMillis != 0L) getTimeAgo(timestampMillis) else ""


            itemTitle.text = item.productName
            itemExchange.text = item.requestedProduct
            itemLocation.text = item.location
        }
        holder.binding.root.setOnClickListener {
            if (!item.ownerId.isNullOrEmpty()) {
                // تحويل Base64 لكل الصور إلى ملفات مؤقتة
                val imagePaths = arrayListOf<String>()
                item.images.forEachIndexed { i, base64 ->
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val tempFile = File(activity.cacheDir, "offer_${item.offerId}_$i.jpg")
                        FileOutputStream(tempFile).use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                        }
                        imagePaths.add(tempFile.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val intent = Intent(activity, Offer_Details_general::class.java)
                intent.putExtra("offerId", item.offerId)
                intent.putExtra("productName", item.productName)
                intent.putExtra("ownerName", item.ownerName)
                intent.putExtra("requestedProduct", item.requestedProduct)
                intent.putExtra("description", item.description)
                intent.putExtra("location", item.location)
                intent.putExtra("category", item.category)
                intent.putExtra("postTimestampMillis", item.createdAt?.toDate()?.time ?: 0L)
                intent.putExtra("ownerId", item.ownerId)
                intent.putStringArrayListExtra("imagesPaths", imagePaths)
                intent.putExtra("requesterId", requesterId)
                intent.putExtra("requesterName", requesterName)

                Log.d("CardAdapter", "ownerId=${item.ownerId}, offerId=${item.offerId}")
                activity.startActivity(intent)
            } else {
                // ownerId غير موجود، نعرض رسالة بدل أن نسبب crash
                Toast.makeText(activity, "بيانات المالك غير متوفرة", Toast.LENGTH_SHORT).show()
                Log.e("CardAdapter", "ownerId is null for offerId=${item.offerId}")
            }
        }

    }
    override fun getItemCount() = itemList.size

}

// نفس الدالة في نفس الملف أو يوتيل مشترك
private fun decodeBase64ToBitmapSafe(b64: String?): Bitmap? {
    if (b64.isNullOrBlank()) return null
    return try {
        val payload = if (b64.startsWith("data:", true)) b64.substringAfter(',') else b64
        val clean = payload.replace("\\s".toRegex(), "")
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) { null }
}
private fun getTimeAgo(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMillis

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "قبل لحظات"
        diff < TimeUnit.HOURS.toMillis(1) -> "منذ $minutes دقيقة"
        diff < TimeUnit.DAYS.toMillis(1) -> "منذ $hours ساعة"
        else -> "منذ $days يوم"
    }
}