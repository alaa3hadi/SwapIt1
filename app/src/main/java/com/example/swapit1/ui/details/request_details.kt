package com.example.swapit1.ui.details

import DeleteRequestWorker
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.databinding.ActivityRequestDetailsBinding
import com.example.swapit1.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import me.relex.circleindicator.CircleIndicator3
import java.util.concurrent.TimeUnit

class request_details : AppCompatActivity() {
    private var _binding: ActivityRequestDetailsBinding? = null
    private val binding get() = _binding!!
    // Firebase (بدون lazy)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        _binding = ActivityRequestDetailsBinding.inflate(layoutInflater)
        _binding?.backButton?.setOnClickListener {
            finish()
        }

        // استلام البيانات

        val productName = intent.getStringExtra("productName")
        val productId = intent.getStringExtra("productId")?: "123"

        val requestedProduct = intent.getStringExtra("correspondingProduct")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val category = intent.getStringExtra("category")
        val ownerId = intent.getStringExtra("ownerId")?: "12345"
        val ownerName = intent.getStringExtra("ownerName")
        val requestId = intent.getStringExtra("requestId")
        val requesterId = intent.getStringExtra("requesterId")?: "12345"
        val requesterName = intent.getStringExtra("requesterName")
        val state = intent.getStringExtra("State")

        val imagesPaths = intent.getStringArrayListExtra("imagesPaths") ?: arrayListOf<String>()

        // تعيين النصوص
        binding.productName.text = productName
        binding.productDescription.text = description
        binding.productLocation.text = location
        binding.productCategory.text = category

        db.collection("users").document(requesterId)
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
binding.btnCall.setOnClickListener {
    db.collection("users").document(ownerId)
        .addSnapshotListener { doc, _ ->
            val phone = (doc?.getString("phone") ?: "1234").ifBlank { "1234" }
            if (!phone.isNullOrEmpty()) {
                try {
                    val url = "https://wa.me/$phone"  // صيغة الواتساب الرسمية
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setPackage("com.whatsapp") // يفتح الواتس فقط
                    intent.data = android.net.Uri.parse(url)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "الواتساب غير مثبت على هذا الجهاز", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
            }


        }
}

        binding.btnMessage.setOnClickListener {
            db.collection("users").document(ownerId)
                .addSnapshotListener { doc, _ ->
                    val phone = (doc?.getString("phone") ?: "1234").ifBlank { "1234" }
                    if (!phone.isNullOrEmpty()) {
                        try {
                            val url = "https://wa.me/$phone"  // صيغة الواتساب الرسمية
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setPackage("com.whatsapp") // يفتح الواتس فقط
                            intent.data = android.net.Uri.parse(url)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "الواتساب غير مثبت على هذا الجهاز", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
                    }


                }
        }

        binding.btnAccept.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_accept_request, null)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnConfirm.setOnClickListener {
                alertDialog.dismiss()

                requestId?.let { reqId ->
                    // أولاً: قبول الطلب الأساسي
                    db.collection("requests").document(reqId)
                        .update("state", RequestState.ACCEPTED)
                        .addOnSuccessListener {
                          Log.e("productIdd" , "${productId}")
                            // ثانياً: رفض باقي الطلبات
                            db.collection("requests")
                                .whereEqualTo("productId", productId)
                                .get()
                                .addOnSuccessListener { query ->
                                    for (doc in query.documents) {
                                        if (doc.id != reqId) {
                                            doc.reference.update("state", RequestState.REJECTED)
                                            // جدول Worker لكل طلب مرفوض
                                            val data = Data.Builder().putString("requestId", doc.id).build()
                                            val work = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                                                .setInitialDelay(2, TimeUnit.MINUTES)
                                                .setInputData(data)
                                                .build()
                                            WorkManager.getInstance(this).enqueue(work)
                                        }
                                    }
                                }

                            // ثالثاً: حذف العرض من الـ offers
                            Log.e("offerIdd" , "${productId}")

                            db.collection("offers").document(productId)
                                .delete()
                                .addOnSuccessListener {
                                    // نجاح حذف العرض
                                    Toast.makeText(this, "تم حذف العرض: ", Toast.LENGTH_SHORT).show()

                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "خطأ عند حذف العرض: ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                            // جدول Worker للطلب المقبول بعد 2 دقيقة
                            val dataAccepted = Data.Builder().putString("requestId", reqId).build()
                            val workAccepted = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                                .setInitialDelay(2, TimeUnit.MINUTES)
                                .setInputData(dataAccepted)
                                .build()
                            WorkManager.getInstance(this).enqueue(workAccepted)


                            // خامساً: إظهار نافذة نجاح
                            val successView = LayoutInflater.from(this).inflate(R.layout.dialog_success_toast, null)
                            val successDialog = AlertDialog.Builder(this)
                                .setView(successView)
                                .create()
                            successDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            successDialog.setCanceledOnTouchOutside(false)
                            successDialog.window?.setDimAmount(0.6f)
                            successDialog.setOnShowListener {
                                val width = (this.resources.displayMetrics.widthPixels * 0.85).toInt()
                                successDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                            }
                            successDialog.show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                if (successDialog.isShowing) successDialog.dismiss()
                                finish()
                            }, 3000)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "خطأ عند تحديث الطلب: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }


            btnCancel.setOnClickListener { alertDialog.dismiss() }
            alertDialog.show()
        }

        binding.btnReject.setOnClickListener {
            val dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_reject_request, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

            btnCancel.setOnClickListener { alertDialog.dismiss() }
            btnConfirm.setOnClickListener {
                alertDialog.dismiss()
                requestId?.let { docId ->
                    db.collection("requests").document(docId)
                        .update("state", RequestState.REJECTED.name)
                        .addOnSuccessListener {
                            // حذف الطلب بعد 24 ساعة
                            Handler(Looper.getMainLooper()).postDelayed({
                                db.collection("requests").document(docId).delete()
                            }, 2 * 60 * 1000 // دقيقتين
                            )

                            Toast.makeText(this, "تم رفض الطلب", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
            }
            alertDialog.show()
        }


        setContentView(binding.root)



    }
}