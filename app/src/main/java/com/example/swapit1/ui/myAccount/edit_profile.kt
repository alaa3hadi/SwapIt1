package com.example.swapit1.ui.myAccount

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityEditProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class edit_profile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private var isImageDeleted = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageUri = uri
                    isImageDeleted = false
                    binding.profileImage.setImageURI(uri)
                    binding.changePhotoText.text = "تغيير الصورة"
                }
            } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationDropdown()
        loadUserDataFromFirebase()

        binding.profileImage.setOnClickListener { onProfileImageClicked() }
        binding.changePhotoText.setOnClickListener { onProfileImageClicked() }
        binding.backButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { saveProfileToFirebase() }
    }

    private fun setupLocationDropdown() {
        val locationDropdown = findViewById<AutoCompleteTextView>(R.id.locationDropdown)
        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        locationDropdown.setAdapter(adapter)
        locationDropdown.setOnClickListener { locationDropdown.showDropDown() }
    }

    private fun onProfileImageClicked() {
        if (binding.changePhotoText.text == "إضافة صورة") {
            pickImageFromCameraOrGallery()
        } else {
            val options = arrayOf("تغيير الصورة", "حذف الصورة")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("اختر خيار")
                .setItems(options) { _, which ->
                    when (options[which]) {
                        "تغيير الصورة" -> pickImageFromCameraOrGallery()
                        "حذف الصورة" -> {
                            imageUri = null
                            isImageDeleted = true
                            binding.profileImage.setImageResource(R.drawable.user_icon)
                            binding.changePhotoText.text = "إضافة صورة"
                        }
                    }
                }.show()
        }
    }

    private fun pickImageFromCameraOrGallery() {
        val options = arrayOf("كاميرا", "معرض الصور")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("اختر مصدر الصورة")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ImagePicker.with(this)
                        .cameraOnly()
                        .cropSquare()
                        .maxResultSize(1080, 1080)
                        .createIntent { intent -> pickImageLauncher.launch(intent); null }
                    1 -> ImagePicker.with(this)
                        .galleryOnly()
                        .cropSquare()
                        .maxResultSize(1080, 1080)
                        .createIntent { intent -> pickImageLauncher.launch(intent); null }
                }
            }.show()
    }

    private fun loadUserDataFromFirebase() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    binding.nameEditText.setText(doc.getString("name") ?: "")
                    binding.phoneEditText.setText(doc.getString("phone") ?: "")
                    binding.locationDropdown.setText(doc.getString("location") ?: "", false)

                    val photoBase64 = doc.getString("photoBase64")
                    if (!photoBase64.isNullOrEmpty()) {
                        val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.profileImage.setImageBitmap(bitmap)
                        binding.changePhotoText.text = "تغيير الصورة"
                    } else {
                        binding.changePhotoText.text = "إضافة صورة"
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "فشل تحميل البيانات: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileToFirebase() {
        val currentUser = auth.currentUser ?: return

        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val location = binding.locationDropdown.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "location" to location
        )

        if (isImageDeleted) {
            updates["photoBase64"] = "" // حذف الصورة من الفايرستور
        } else {
            imageUri?.let { uri ->
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val photoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                updates["photoBase64"] = photoBase64
            }
        }

        firestore.collection("users").document(currentUser.uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "فشل حفظ التعديلات: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
