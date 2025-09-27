package com.example.swapit1.ui.details

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityOfferDetailsBinding
import com.example.swapit1.databinding.ActivityOfferDetailsGeneralBinding
import com.example.swapit1.ui.SpecificProductRequests
import com.example.swapit1.ui.myAccount.contact_us
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import me.relex.circleindicator.CircleIndicator3
import java.util.*
import java.util.concurrent.TimeUnit


class offer_details : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityOfferDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfferDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // زر الرجوع
        binding.backButton.setOnClickListener { finish() }
        binding.btnDisplayReq.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SpecificProductRequests::class.java
                )
            )
        }
        // استلام البيانات
        val productName = intent.getStringExtra("productName")
        val requestedProduct = intent.getStringExtra("requestedProduct")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val category = intent.getStringExtra("category")
        val ownerId = intent.getStringExtra("ownerId") ?: "12345"
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
        loadOwnerData(ownerId)
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

    private fun loadOwnerData(ownerId: String?) {
        db.collection("users").document(ownerId.toString())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val ownerName = doc.getString("name") ?: "غير معروف"
                    val ownerPhone = doc.getString("phone") ?: "غير متوفر"
                    val photoBase64 = doc.getString("photoBase64")

                    binding.userName.text = ownerName
                    binding.userPhone.text = ownerPhone

                    if (!photoBase64.isNullOrEmpty()) {
                        val bytes =
                            android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.userImage.setImageBitmap(bitmap)
                    } else {
                        binding.userImage.setImageResource(R.drawable.user_icon)
                    }

                    // أزرار الاتصال والمراسلة
                    binding.btnCall.setOnClickListener {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$ownerPhone"))
                        startActivity(callIntent)
                    }
                    binding.btnMessage.setOnClickListener {
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$ownerPhone"))
                        startActivity(smsIntent)
                    }

                } else {
                    Toast.makeText(this, "بيانات المالك غير متوفرة", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل تحميل بيانات المالك", Toast.LENGTH_SHORT).show()
            }
    }


}
