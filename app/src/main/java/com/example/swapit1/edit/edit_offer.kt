package com.example.swapit1.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityEditOfferBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class edit_offer : AppCompatActivity() {

    private lateinit var binding: ActivityEditOfferBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val selectedImages = mutableListOf<Uri>() // صور جديدة
    private val allImages = mutableListOf<Any>() // صور قديمة Base64 أو Bitmaps
    private val bitmaps = mutableListOf<Any>() // للعرض فقط

    private lateinit var pagerAdapter: SelectedImagesPagerAdapter
    private var offerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // استلام البيانات من Intent
        offerId = intent.getStringExtra("offerId")
        binding.productNameNeedEditText.setText(intent.getStringExtra("productName") ?: "")
        binding.productNameEditText.setText(intent.getStringExtra("requestedProduct") ?: "")
        binding.descriptionEditText.setText(intent.getStringExtra("description") ?: "")
        binding.categorySpinner.setText(intent.getStringExtra("category") ?: "-اختر-", false)
        binding.locationSpinner.setText(intent.getStringExtra("location") ?: "-اختر-", false)

        // تحميل الصور القديمة من الملفات
        val imagesPaths = intent.getStringArrayListExtra("imagesPaths") ?: arrayListOf()
        imagesPaths.forEach { path ->
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                bitmaps.add(bitmap)
                allImages.add(bitmap)
            }
        }

        setupSpinners()
        setupViewPager()

        binding.addPhotoLayout.setOnClickListener { openImagePicker() }
        binding.bottom.setOnClickListener { updateOffer() }
    }

    private fun setupSpinners() {
        val categories = listOf("طعام", "ملابس", "اطفال", "الكترونيات", "اثاث", "اخرى")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        binding.locationSpinner.setAdapter(locationAdapter)

        binding.categorySpinner.setOnClickListener { binding.categorySpinner.showDropDown() }
        binding.locationSpinner.setOnClickListener { binding.locationSpinner.showDropDown() }
    }

    private fun setupViewPager() {
        pagerAdapter = SelectedImagesPagerAdapter(
            images = bitmaps,
            onRemoveClick = { item ->
                bitmaps.remove(item)
                when(item) {
                    is Uri -> selectedImages.remove(item)
                    is Bitmap -> allImages.remove(item)
                }
                pagerAdapter.notifyDataSetChanged()
                // تحديث indicator بعد التغيير
                binding.indicator.setViewPager(binding.viewPagerImages)
            },
            onAddClick = { openImagePicker() }
        )

        binding.viewPagerImages.adapter = pagerAdapter
        binding.viewPagerImages.visibility = View.VISIBLE
        binding.indicator.visibility = View.VISIBLE
        binding.indicator.setViewPager(binding.viewPagerImages)
    }

    private val imagePickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImages.add(uri)

                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }

                bitmaps.add(bitmap)
                pagerAdapter.notifyDataSetChanged()
                binding.indicator.setViewPager(binding.viewPagerImages) // تحديث الـ indicator

            }
        } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val options = arrayOf("كاميرا", "معرض الصور")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("اختر مصدر الصورة")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ImagePicker.with(this)
                        .cameraOnly()
                        .cropSquare()
                        .maxResultSize(1080, 1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }
                    1 -> ImagePicker.with(this)
                        .galleryOnly()
                        .cropSquare()
                        .maxResultSize(1080, 1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }
                }
            }.show()
    }

    private fun updateOffer() {
        val productName = binding.productNameNeedEditText.text.toString().trim()
        val requestedProduct = binding.productNameEditText.text.toString().trim()
        val category = binding.categorySpinner.text.toString()
        val location = binding.locationSpinner.text.toString()
        val description = binding.descriptionEditText.text.toString().trim()

        if (productName.isEmpty() || requestedProduct.isEmpty() || category.isEmpty() ||
            location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "الرجاء تعبئة كل الحقول", Toast.LENGTH_SHORT).show()
            return
        }

        // تحويل الصور القديمة إلى Base64 إذا كانت Bitmaps
        val encodedOldImages = allImages.map { image ->
            when (image) {
                is Bitmap -> {
                    val baos = ByteArrayOutputStream()
                    image.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                }
                is String -> image // إذا كانت Base64 بالفعل
                else -> null
            }
        }.filterNotNull()

        val imageStrings = encodedOldImages.toMutableList()

        // معالجة الصور الجديدة من Uri
        selectedImages.forEach { uri ->
            try {
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                imageStrings.add(encodedImage)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "فشل معالجة صورة", Toast.LENGTH_SHORT).show()
            }
        }

        val updates = mapOf(
            "productName" to productName,
            "requestedProduct" to requestedProduct,
            "category" to category,
            "location" to location,
            "description" to description,
            "images" to imageStrings
        )

        offerId?.let {
            firestore.collection("offers").document(it)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "تم تعديل العرض ✅", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "فشل التعديل: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
