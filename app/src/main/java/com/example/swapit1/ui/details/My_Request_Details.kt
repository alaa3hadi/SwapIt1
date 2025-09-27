package com.example.swapit1.ui.details

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.databinding.FragmentHomeBinding
import com.example.swapit1.model.RequestState
import com.example.swapit1.model.requestItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import me.relex.circleindicator.CircleIndicator3
import java.util.concurrent.TimeUnit

class My_Request_Details : AppCompatActivity() {
    private var _binding: ActivityMyRequestDetailsBinding? = null
    private val binding get() = _binding!!
    // Firebase (بدون lazy)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
          _binding = ActivityMyRequestDetailsBinding.inflate(layoutInflater)
        _binding?.backButton?.setOnClickListener {
            finish()
        }

        // استلام البيانات

        val productName = intent.getStringExtra("productName")
        val requestedProduct = intent.getStringExtra("correspondingProduct")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val category = intent.getStringExtra("category")
        val ownerId = intent.getStringExtra("ownerId")?: "12345"
        val ownerName = intent.getStringExtra("ownerName")
        val requestId = intent.getStringExtra("requestId")
        val requesterId = intent.getStringExtra("requesterId")
        val requesterName = intent.getStringExtra("requesterName")
        val state = intent.getSerializableExtra("State") as RequestState

        val imagesPaths = intent.getStringArrayListExtra("imagesPaths") ?: arrayListOf<String>()

        // تعيين النصوص
        binding.productName.text = productName
        binding.productDescription.text = description
        binding.productLocation.text = location
        binding.productCategory.text = category
       val currState =  binding.currentState


        val statusBackground = currState.background as GradientDrawable

        when (state) {
            RequestState.PENDING -> {
                currState.text = "قيد الانتظار"
                currState.setTextColor(this.getColor(R.color.blue))
                statusBackground.setStroke(2, this.getColor(R.color.blue))  // تغيير لون الحدود
            }
            RequestState.ACCEPTED -> {
                currState.text = "تم القبول"
                currState.setTextColor(this.getColor(R.color.green))
                statusBackground.setStroke(2, this.getColor(R.color.green))
            }
            RequestState.REJECTED -> {
                currState.text = "تم الرفض"
                currState.setTextColor(this.getColor(R.color.red))
                statusBackground.setStroke(2, this.getColor(R.color.red))
            }
        }


        db.collection("users").document(ownerId)
            .addSnapshotListener { doc, _ ->
                if (_binding == null) return@addSnapshotListener
                val phone = (doc?.getString("phone") ?: "1234").ifBlank { "1234" }
                val name = (doc?.getString("name") ?: "Sara").ifBlank { "sara" }

                    binding.userName.text = "$name"
                binding.userPhone.text = "$phone"

            }



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

        loadOwnerData(requesterId)


        setContentView(binding.root)



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
                        val bytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
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