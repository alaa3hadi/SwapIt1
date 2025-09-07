package com.example.swapit1.ui.details

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityOfferDetailsBinding
import com.google.firebase.Timestamp
import me.relex.circleindicator.CircleIndicator3
import java.util.*
import java.util.concurrent.TimeUnit

class offer_details : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityOfferDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // زر الرجوع
        binding.backButton.setOnClickListener { finish() }

        // استلام البيانات
        val productName = intent.getStringExtra("productName")
        val requestedProduct = intent.getStringExtra("requestedProduct")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val category = intent.getStringExtra("category")

//        val timestamp = intent.getSerializableExtra("postTimestamp") as? Timestamp
        val imagesPaths = intent.getStringArrayListExtra("imagesPaths") ?: arrayListOf<String>()

        // تعيين النصوص
        binding.textProductName.text = productName
        binding.textRequestedProduct.text = requestedProduct
        binding.textDescription.text = description
        binding.textLocation.text = location
        binding.textCategory.text = category


        val timestampMillis = intent.getLongExtra("postTimestampMillis", 0L)
        binding.textPostDate.text = if (timestampMillis != 0L) getTimeAgo(timestampMillis) else ""


        // تحويل مسارات الصور ل Bitmaps
        val imageBitmaps = imagesPaths.mapNotNull { path ->
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // إعداد Adapter للصور
        val adapter = SelectedImagesPagerAdapter(imageBitmaps)
        binding.viewPagerImages.adapter = adapter

        // ربط CircleIndicator3 مع ViewPager2
        val indicator: CircleIndicator3 = binding.indicator
        indicator.setViewPager(binding.viewPagerImages)
    }

    // دالة لتحويل Timestamp لصيغة "منذ يوم/ساعة/دقيقة"
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

}
