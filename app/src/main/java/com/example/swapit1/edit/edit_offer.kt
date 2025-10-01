package com.example.swapit1.edit

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityEditOfferBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class edit_offer : AppCompatActivity() {

    private lateinit var binding: ActivityEditOfferBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var loadingDialog: AlertDialog? = null
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

        binding.backButton.setOnClickListener { finish() }

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
        showLoadingDialog()

        offerId?.let {
            firestore.collection("offers").document(it)
                .update(updates)
                .addOnSuccessListener {
                    hideLoadingDialog()
                    showSuccessDialog()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "فشل التعديل: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoadingDialog() {
        val progressBar = ProgressBar(this).apply { isIndeterminate = true }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            addView(progressBar)
            addView(TextView(this@edit_offer).apply {
                text = "جاري تعديل العرض..."
                textSize = 18f
                setTextColor(android.graphics.Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(0, 20, 0, 0)
            })
        }
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(container)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showSuccessDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        // 🔹 Lottie Animation
        val animationView = com.airbnb.lottie.LottieAnimationView(this).apply {
            setAnimation(R.raw.success) // ملفك داخل res/raw
            repeatCount = 0             // يشتغل مرة وحدة فقط
            playAnimation()
            layoutParams = LinearLayout.LayoutParams(400, 400).apply {
                gravity = Gravity.CENTER
            }
        }

        // 🔹 نص تحت الأنيميشن
        val message = TextView(this).apply {
            text = "تم تعديل العرض بنجاح"
            textSize = 20f
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
        }

        container.addView(animationView)
        container.addView(message)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(container)
            .setCancelable(false) // ما يقدر يسكر يدوي
            .create()

        dialog.show()

        // 🔹 بعد ما يخلص الأنيميشن -> يسكر ويرجع
        animationView.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator) {
                dialog.dismiss()
                finish()
            }

            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }
}
