package com.example.swapit1.ui.addOffer

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.FragmentAddOfferBinding
import com.example.swapit1.model.Offers
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.navigation.fragment.findNavController
import com.example.swapit1.R
import android.widget.TextView
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout
import android.view.Gravity
import android.widget.ProgressBar

class addOfferFragment : Fragment() {

    private var _binding: FragmentAddOfferBinding? = null
    private val binding get() = _binding!!

    private val selectedImages = mutableListOf<Uri>()
    private lateinit var pagerAdapter: SelectedImagesPagerAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddOfferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupViewPager()
        setupListeners()
        setupErrorListeners()
    }

    private fun setupSpinners() {
        val categories = listOf("طعام", "ملابس", "اطفال", "الكترونيات", "اثاث", "اخرى")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locations)
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

    private fun setupListeners() {
        binding.bottom.setOnClickListener { publishOffer() }
    }

    private fun setupErrorListeners() {
        binding.productNameEditText.addTextChangedListener {
            binding.productNameError.visibility = View.GONE
        }
        binding.requestedProductEditText.addTextChangedListener {
            binding.requestedProductError.visibility = View.GONE
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
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

    private val imagePickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImages.add(uri)
                pagerAdapter.notifyDataSetChanged()
                refreshIndicator()
            }
        } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
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

    private fun publishOffer() {
        val productName = binding.productNameEditText.text.toString().trim()
        val requestedProduct = binding.requestedProductEditText.text.toString().trim()
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

        if (requestedProduct.isEmpty()) {
            binding.requestedProductError.text = "يرجى إدخال اسم المنتج المطلوب"
            binding.requestedProductError.visibility = View.VISIBLE
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

        uploadImagesAndSaveOffer(productName, requestedProduct, category, location, description)
    }

    private fun uploadImagesAndSaveOffer(
        productName: String,
        requestedProduct: String,
        category: String,
        location: String,
        description: String
    ) {
        showLoadingDialog()

        val imageStrings = mutableListOf<String>()

        selectedImages.forEach { uri ->
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            imageStrings.add(encodedImage)
        }

        saveOfferToFirestore(productName, requestedProduct, category, location, description, imageStrings)
    }

    private fun saveOfferToFirestore(
        productName: String,
        requestedProduct: String,
        category: String,
        location: String,
        description: String,
        images: List<String>
    ) {
        val currentUser = auth.currentUser
        val offersCollection = firestore.collection("offers")
        val newDocRef = offersCollection.document()
        val offerId = newDocRef.id
        firestore.collection("users").document(currentUser?.uid ?: "user123")
            .addSnapshotListener { doc, _ ->
                if (_binding == null) return@addSnapshotListener
                val name = (doc?.getString("name") ?: "مستخدم").ifBlank { "مستخدم" }
                val offer = Offers(
                    offerId = offerId,
                    ownerId = currentUser?.uid ?: "user123",
                    ownerName = name ?: "Sara Abu Kwaik",
                    productName = productName,
                    requestedProduct = requestedProduct,
                    category = category,
                    location = location,
                    description = description,
                    images = images,
                    createdAt = com.google.firebase.Timestamp.now()
                )
                newDocRef.set(offer)
                    .addOnSuccessListener {
                        hideLoadingDialog()
                        showSuccessDialog()
                    }
                    .addOnFailureListener { e ->
                        hideLoadingDialog()
                        Toast.makeText(requireContext(), "فشل نشر العرض: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }




    }


    private fun showLoadingDialog() {
        val progressBar = ProgressBar(requireContext()).apply { isIndeterminate = true }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            addView(progressBar)
            addView(TextView(requireContext()).apply {
                text = "جاري نشر العرض..."
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 20, 0, 0)
                gravity = Gravity.CENTER
            })
        }

        loadingDialog = MaterialAlertDialogBuilder(requireContext())
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
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 20)
        }

        val message = TextView(requireContext()).apply {
            text = "🎉 تم نشر العرض بنجاح!\n\nهل ترغب بالذهاب إلى الصفحة الرئيسية؟"
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        container.addView(message)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(container)
            .setPositiveButton("الذهاب إلى الرئيسية") { _, _ ->
                clearForm()
                val action = addOfferFragmentDirections.actionAddOfferFragmentToHomeFragment()
                findNavController().navigate(action)
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

    private fun clearForm() {
        selectedImages.clear()
        pagerAdapter.notifyDataSetChanged()
        refreshIndicator()
        binding.categorySpinner.setText("-اختر-")
        binding.locationSpinner.setText("-اختر-")
        binding.productNameEditText.text?.clear()
        binding.requestedProductEditText.text?.clear()
        binding.descriptionEditText.text?.clear()

        binding.productNameError.visibility = View.GONE
        binding.requestedProductError.visibility = View.GONE
        binding.categoryError.visibility = View.GONE
        binding.locationError.visibility = View.GONE
        binding.descriptionError.visibility = View.GONE
        binding.imageError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
