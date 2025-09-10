package com.example.swapit1.ui.details

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.databinding.FragmentHomeBinding
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
        val imagesPaths = intent.getStringArrayListExtra("imagesPaths") ?: arrayListOf<String>()

        // تعيين النصوص
        binding.productName.text = productName
        binding.productDescription.text = description
        binding.productLocation.text = location
        binding.productCategory.text = category

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


        setContentView(binding.root)



    }

}