package com.example.swapit1.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R
import com.example.swapit1.databinding.ItemCardBinding
import com.example.swapit1.databinding.ItemSearchResultBinding
import com.example.swapit1.model.Search
import com.example.swapit1.ui.details.Offer_Details_general
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * يعرض نتائج البحث بنفس كرت all (ItemCardBinding) لكن يحافظ على نفس البيانات:
 * - العنوان: productName
 * - السطر الثاني: "الوصف: description"
 * - الموقع: location
 * - الوقت: من createdAt
 * - الصورة: أول عنصر من images (Base64)
 *
 * ويفتح شاشة التفاصيل عند الضغط، ويمرّر id كـ offerId.
 */
class SearchCardAdapter(
    private val activity: Activity,
    private val items: MutableList<Search> = mutableListOf(),
    private val requesterId: String,
    private val requesterName: String
) : RecyclerView.Adapter<SearchCardAdapter.VH>() {

    fun submit(newItems: List<Search>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemSearchResultBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding

        // الصورة: أول Base64 من images
        val firstB64 = item.images?.firstOrNull()
        val bmp = decodeBase64ToBitmapSafe(firstB64)
        if (bmp != null) {
            b.productImage.setImageBitmap(bmp)
        } else {
            b.productImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        // الوقت
        val tsMillis = item.createdAt?.toDate()?.time ?: 0L
        b.itemTime.text = if (tsMillis != 0L) getTimeAgo(tsMillis) else ""

        // العنوان + "الوصف" في السطر الثاني (نفس بيانات السيرش بدون إضافة حقول جديدة)
        b.itemTitle.text = item.productName.ifBlank { "—" }
        //val desc = item.description.ifBlank { "—" }
        b.itemExchange.text = item.requestedProduct

        // الموقع
        b.locationText.text = item.location ?: "—"

        // فتح التفاصيل
        b.root.setOnClickListener {
            // حوّل كل الصور إلى ملفات مؤقتة (زي CardAdapter)
            val imagePaths = arrayListOf<String>()
            item.images?.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val tempFile = File(activity.cacheDir, "offer_${item.id ?: "unknown"}_$i.jpg")
                    FileOutputStream(tempFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    }
                    imagePaths.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Log.d("SearchCardAdapter", "open details: offerId=${item.id}")

            val intent = Intent(activity, Offer_Details_general::class.java).apply {
                putExtra("offerId", item.id ?: "")                // من Search.id
                putExtra("productName", item.productName)
                putExtra("ownerName", "")                         // غير متوفر في Search
                putExtra("requestedProduct", item.requestedProduct)
                putExtra("description", item.description)
                putExtra("location", item.location ?: "—")
                putExtra("category", item.category ?: "—")
                putExtra("postTimestampMillis", tsMillis)

                putExtra("ownerId", "")                           // غير متوفر في Search
                putStringArrayListExtra("imagesPaths", imagePaths)

                putExtra("requesterId", requesterId)
                putExtra("requesterName", requesterName)
            }
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}

/* Helpers نفس CardAdapter */

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
