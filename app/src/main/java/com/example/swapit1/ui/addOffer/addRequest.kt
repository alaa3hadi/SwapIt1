package com.example.swapit1.ui.addOffer

import android.R
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.graphics.Color
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.swapit1.databinding.ActivityAddRequestBinding
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.model.Request
import com.example.swapit1.model.RequestState
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class addRequest : AppCompatActivity() {

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
        binding.backButton.setOnClickListener {
            finish()
        }
        val ownerId = intent.getStringExtra("ownerId")
        val offerId = intent.getStringExtra("offerId")
        val requesterId = intent.getStringExtra("requesterId")
        val ownerName = intent.getStringExtra("ownerName")
        val requesterName = intent.getStringExtra("requesterName")
        val correspondingProduct = intent.getStringExtra("productName")



        setupSpinners()
        setupViewPager()
        setupListeners(ownerId ?: "" , ownerName ?: ""  , offerId ?: "" , requesterId ?: "" , requesterName ?: "" , correspondingProduct ?: "")
        setupErrorListeners()
        binding.bottom
    }

    private fun setupSpinners() {
        val categories = listOf("طعام", "ملابس", "اطفال", "الكترونيات", "اثاث", "اخرى")
        val categoryAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        val locationAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, locations)
        binding.locationSpinner.setAdapter(locationAdapter)

        binding.categorySpinner.setOnClickListener { binding.categorySpinner.showDropDown() }
        binding.locationSpinner.setOnClickListener { binding.locationSpinner.showDropDown() }
    }

    private fun setupViewPager() {
        pagerAdapter = SelectedImagesPagerAdapter(
            images = selectedImages,
            onRemoveClick = { uri ->
                selectedImages.remove(uri)
                pagerAdapter.notifyDataSetChanged()
                refreshIndicator()
            },
            onAddClick = { openImagePicker() }
        )
        binding.viewPagerImages.adapter = pagerAdapter
        binding.indicator.setViewPager(binding.viewPagerImages)
        binding.addPhotoLayout.setOnClickListener {
            binding.imageError.visibility = View.GONE
            openImagePicker()
        }
        refreshIndicator()
    }

    private fun setupListeners(ownerId : String , ownerName : String , offerId : String , requesterId :String , requesterName: String , correspondingProduct : String) {
        binding.bottom.setOnClickListener { publishRequest(ownerId , ownerName , offerId , requesterId , requesterName , correspondingProduct  ) }
    }
    private fun setupErrorListeners() {
        binding.productNameEditText.addTextChangedListener {
            binding.productNameError.visibility = View.GONE
        }

        binding.categorySpinner.setOnItemClickListener { _, _, _, _ ->
            binding.categoryError.visibility = View.GONE
            binding.categorySpinner.setTextColor(Color.BLACK)
        }
        binding.locationSpinner.setOnItemClickListener { _, _, _, _ ->
            binding.locationError.visibility = View.GONE
            binding.locationSpinner.setTextColor(Color.BLACK)
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
                    0 -> ImagePicker.with(this).cameraOnly().cropSquare().maxResultSize(1080,1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }
                    1 -> ImagePicker.with(this).galleryOnly().cropSquare().maxResultSize(1080,1080)
                        .createIntent { intent -> imagePickerLauncher.launch(intent); null }
                }
            }.show()
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImages.add(uri)
                pagerAdapter.notifyDataSetChanged()
                refreshIndicator()
            }
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

    private fun publishRequest(ownerId : String , ownerName : String , offerId : String , requesterId : String , requesterName: String , correspondingProduct : String) {
        val productName = binding.productNameEditText.text.toString().trim()
        val category = binding.categorySpinner.text.toString()
        val location = binding.locationSpinner.text.toString()
        val description = binding.descriptionEditText.text.toString().trim()

        var isValid = true
        if (selectedImages.isEmpty()) { binding.imageError.text="يرجى إضافة صورة واحدة على الأقل"; binding.imageError.visibility = View.VISIBLE; isValid=false }
        if (productName.isEmpty()) { binding.productNameError.text="يرجى إدخال اسم الطلب"; binding.productNameError.visibility = View.VISIBLE; isValid=false }
        if (category=="-اختر-") { binding.categoryError.text="يرجى اختيار القسم"; binding.categoryError.visibility = View.VISIBLE; isValid=false }
        if (location=="-اختر-") { binding.locationError.text="يرجى اختيار الموقع"; binding.locationError.visibility = View.VISIBLE; isValid=false }
        if (description.isEmpty()) { binding.descriptionError.text="يرجى إدخال الوصف"; binding.descriptionError.visibility = View.VISIBLE; isValid=false }
        if (!isValid) return

        uploadImagesAndSaveRequest(productName, category, location, description , ownerId , ownerName , offerId , requesterId , requesterName  , correspondingProduct)
    }

    private fun uploadImagesAndSaveRequest(productName: String, category: String, location: String, description: String ,  ownerId : String , ownerName : String , offerId : String , requesterId : String , requesterName : String , correspondingProduct : String) {
        showLoadingDialog()
        val imageStrings = mutableListOf<String>()
        selectedImages.forEach { uri ->
            val bitmap: Bitmap = if(Build.VERSION.SDK_INT>=29){
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos)
            val encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            imageStrings.add(encodedImage)
        }
        saveRequestToFirestore(productName, category, location, description, imageStrings , ownerId , ownerName , offerId , requesterId , requesterName , correspondingProduct )
    }

    private fun saveRequestToFirestore(
        productName: String,
        category: String,
        location: String,
        description: String,
        images: List<String>,
        ownerId : String ,
        ownerName : String ,
        offerId : String ,
        requesterId : String ,
        requesterName : String ,
        correspondingProduct : String
    ) {
        val currentUser = auth.currentUser
        val requestsCollection = firestore.collection("requests")
        val newDocRef = requestsCollection.document()
        val reqId = newDocRef.id




        val request = Request(

            ownerId = ownerId ?: "",
            ownerName = ownerName ?: "",
            requesterId = requesterId ?: "user123",
            requesterName = requesterName,
            productId = offerId ?: "",
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

        requestsCollection.add(request)
            .addOnSuccessListener {

                Log.d("AddRequest", "ownerId=$ownerId, requesterId=$requesterId, productId=$offerId")

                hideLoadingDialog()
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(this, "فشل نشر الطلب: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 20)
        }

        val message = TextView(this).apply {
            text = "🎉 تم نشر الطلب بنجاح!\n\nهل ترغب بالذهاب إلى الصفحة الرئيسية؟"
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        container.addView(message)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(container)
            .setPositiveButton("الذهاب إلى الرئيسية") { _, _ ->
                clearForm()
                finish() // أو تنقلك إلى HomeActivity حسب التطبيق
            }
            .setNegativeButton("البقاء هنا") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(Color.parseColor("#F9BC25"))
            textSize = 18f
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(Color.GRAY)
            textSize = 18f
        }
    }

    private fun showLoadingDialog() {
        val progressBar = ProgressBar(this).apply { isIndeterminate=true }
        val container = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(50,50,50,50)
            addView(progressBar)
            addView(TextView(this@addRequest).apply { text="جاري نشر الطلب..."; textSize=18f; setTextColor(Color.BLACK); setPadding(0,20,0,0); gravity=Gravity.CENTER })
        }
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(container)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog(){ loadingDialog?.dismiss(); loadingDialog=null }

    private fun clearForm(){
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
