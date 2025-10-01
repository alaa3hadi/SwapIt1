package com.example.swapit1.ui.addOffer

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.example.swapit1.NotificationHelper
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityAddRequestBinding
import com.example.swapit1.model.Request
import com.example.swapit1.model.RequestState
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class addRequest : FragmentActivity() {

    private lateinit var binding: ActivityAddRequestBinding
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var pagerAdapter: SelectedImagesPagerAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        val ownerId = intent.getStringExtra("ownerId") ?: ""
        val offerId = intent.getStringExtra("offerId") ?: ""
        val requesterId = intent.getStringExtra("requesterId") ?: ""
        val ownerName = intent.getStringExtra("ownerName") ?: ""
        val requesterName = intent.getStringExtra("requesterName") ?: ""
        val correspondingProduct = intent.getStringExtra("productName") ?: ""

        setupSpinners()
        setupViewPager()
        setupListeners(
            ownerId,
            ownerName,
            offerId,
            requesterId,
            requesterName,
            correspondingProduct
        )
        setupErrorListeners()
    }

    private fun setupSpinners() {
        val categories = listOf("طعام", "ملابس", "اطفال", "الكترونيات", "اثاث", "اخرى")
        binding.categorySpinner.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
            )
        )

        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        binding.locationSpinner.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locations
            )
        )

        binding.categorySpinner.setOnClickListener { binding.categorySpinner.showDropDown() }
        binding.locationSpinner.setOnClickListener { binding.locationSpinner.showDropDown() }
    }

    private fun setupViewPager() {
        pagerAdapter = SelectedImagesPagerAdapter(
            images = selectedImages,
            onRemoveClick = { uri -> selectedImages.remove(uri); pagerAdapter.notifyDataSetChanged(); refreshIndicator() },
            onAddClick = { openImagePicker() }
        )
        binding.viewPagerImages.adapter = pagerAdapter
        binding.indicator.setViewPager(binding.viewPagerImages)
        binding.addPhotoLayout.setOnClickListener {
            binding.imageError.visibility = View.GONE; openImagePicker()
        }
        refreshIndicator()
    }

    private fun setupListeners(
        ownerId: String,
        ownerName: String,
        offerId: String,
        requesterId: String,
        requesterName: String,
        correspondingProduct: String
    ) {
        binding.bottom.setOnClickListener {
            publishRequest(
                ownerId,
                ownerName,
                offerId,
                requesterId,
                requesterName,
                correspondingProduct
            )
        }
    }

    private fun setupErrorListeners() {
        binding.productNameEditText.addTextChangedListener {
            binding.productNameError.visibility = View.GONE
        }
        binding.categorySpinner.setOnItemClickListener { _, _, _, _ ->
            binding.categoryError.visibility = View.GONE; binding.categorySpinner.setTextColor(
            android.graphics.Color.BLACK
        )
        }
        binding.locationSpinner.setOnItemClickListener { _, _, _, _ ->
            binding.locationError.visibility = View.GONE; binding.locationSpinner.setTextColor(
            android.graphics.Color.BLACK
        )
        }
        binding.descriptionEditText.addTextChangedListener {
            binding.descriptionError.visibility = View.GONE
        }
    }

    private fun openImagePicker() {
        val options = arrayOf("كاميرا", "معرض الصور")
        AlertDialog.Builder(this)
            .setTitle("اختر مصدر الصورة")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ImagePicker.with(this).cameraOnly().cropSquare().maxResultSize(1080, 1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }

                    1 -> ImagePicker.with(this).galleryOnly().cropSquare().maxResultSize(1080, 1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }
                }
            }.show()
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri -> selectedImages.add(uri); pagerAdapter.notifyDataSetChanged(); refreshIndicator() }
            } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
            }
        }

    private fun refreshIndicator() {
        if (selectedImages.isEmpty()) {
            binding.viewPagerImages.visibility = View.GONE
            binding.indicator.visibility = View.GONE
            binding.addPhotoLayout.visibility = View.VISIBLE
        } else {
            binding.viewPagerImages.visibility = View.VISIBLE
            binding.indicator.visibility = View.VISIBLE
            binding.addPhotoLayout.visibility = View.GONE
            binding.indicator.setViewPager(binding.viewPagerImages)
        }
    }

    private fun publishRequest(
        ownerId: String,
        ownerName: String,
        offerId: String,
        requesterId: String,
        requesterName: String,
        correspondingProduct: String
    ) {
        val productName = binding.productNameEditText.text.toString().trim()
        val category = binding.categorySpinner.text.toString()
        val location = binding.locationSpinner.text.toString()
        val description = binding.descriptionEditText.text.toString().trim()

        var isValid = true
        if (selectedImages.isEmpty()) {
            binding.imageError.text = "يرجى إضافة صورة واحدة على الأقل"
            binding.imageError.visibility = View.VISIBLE
            isValid = false
        }
        if (productName.isEmpty()) {
            binding.productNameError.text = "يرجى إدخال اسم المنتج"
            binding.productNameError.visibility = View.VISIBLE
            isValid = false
        }
        if (category == "-اختر-") {
            binding.categoryError.text = "يرجى اختيار القسم"
            binding.categoryError.visibility = View.VISIBLE
            isValid = false
        }
        if (location == "-اختر-") {
            binding.locationError.text = "يرجى اختيار الموقع"
            binding.locationError.visibility = View.VISIBLE
            isValid = false
        }
        if (description.isEmpty()) {
            binding.descriptionError.text = "يرجى إدخال الوصف"
            binding.descriptionError.visibility = View.VISIBLE
            isValid = false
        }
        if (!isValid) return

        // رفع الصور Base64
        showLoadingDialog()
        val imageStrings = mutableListOf<String>()
        selectedImages.forEach { uri ->
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            imageStrings.add(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT))
        }

        saveRequestToFirestore(
            ownerId,
            ownerName,
            offerId,
            requesterId,
            requesterName,
            correspondingProduct,
            productName,
            category,
            location,
            description,
            imageStrings
        )
    }

    private fun saveRequestToFirestore(
        ownerId: String,
        ownerName: String,
        offerId: String,
        requesterId: String,
        requesterName: String,
        correspondingProduct: String,
        productName: String,
        category: String,
        location: String,
        description: String,
        images: List<String>
    ) {
        val requestsCollection = firestore.collection("requests")
        val newDocRef = requestsCollection.document()
        val reqId = newDocRef.id
        val request = Request(
            ownerId = ownerId,
            ownerName = ownerName,
            requesterId = requesterId,
            requesterName = requesterName,
            productId = offerId,
            productName = productName,
            correspondingProduct = correspondingProduct,
            category = category,
            location = location,
            description = description,
            images = images,
            createdAt = Timestamp.now(),
            requestId = reqId,
            state = RequestState.PENDING
        )

        newDocRef.set(request)
            .addOnSuccessListener {
                hideLoadingDialog()
                showSuccessDialog()
                // ---------------- إشعار صاحب العرض (Firestore) ----------------
                val notifOwner = hashMapOf(
                    "userId" to ownerId,
                    "title" to "شخص مهتم بعرضك!",
                    "message" to "تحقق من الطلبات الجديدة تواصل مع الطرف الاخر وتتم عملية قبول الطلب بعد عملية نجاح التبديل . ",
                    "type" to "eyes",
                    "createdAt" to Timestamp.now(),
                    "seen" to false
                )
                firestore.collection("notifications").add(notifOwner)

                // ---------------- إشعار صاحب الطلب (Firestore) ----------------
                val notifRequester = hashMapOf(
                    "userId" to requesterId,
                    "title" to "تم إرسال طلبك",
                    "message" to "سيتم إشعارك عند الرد.",
                    "type" to "message",
                    "createdAt" to Timestamp.now(),
                    "seen" to false
                )
                firestore.collection("notifications").add(notifRequester)

                // ---------------- إشعار صاحب الطلب (محلي على الجهاز) ----------------
                NotificationHelper.showNotification(
                    this,
                    "تم إرسال طلبك",
                    "سيتم إشعارك عند الرد.",
                    (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    R.drawable.swapit // 🔹 أيقونة التطبيق أو أيقونة خاصة للطلبات
                )

            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(this, "فشل نشر الطلب: ${e.message}", Toast.LENGTH_SHORT).show()
            }


            }





    private fun showLoadingDialog() {
        val progressBar = ProgressBar(this).apply { isIndeterminate = true }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            addView(progressBar)
            addView(TextView(this@addRequest).apply {
                text = "جاري إرسال الطلب..."
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
            text = "تم نشر الطلب بنجاح"
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
                clearForm()
                finish()
            }

            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }
    private fun clearForm() {
        selectedImages.clear()
        pagerAdapter.notifyDataSetChanged()
        refreshIndicator()
        binding.categorySpinner.setText("-اختر-")
        binding.locationSpinner.setText("-اختر-")
        binding.productNameEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.productNameError.visibility = View.GONE
        binding.categoryError.visibility = View.GONE
        binding.locationError.visibility = View.GONE
        binding.descriptionError.visibility = View.GONE
        binding.imageError.visibility = View.GONE
    }
}
