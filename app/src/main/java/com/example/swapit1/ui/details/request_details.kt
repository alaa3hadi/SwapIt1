 package com.example.swapit1.ui.details

import DeleteRequestWorker
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.swapit1.NotificationHelper
import com.example.swapit1.R
import com.example.swapit1.adapter.SelectedImagesPagerAdapter
import com.example.swapit1.databinding.ActivityRequestDetailsBinding
import com.example.swapit1.model.RequestState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import me.relex.circleindicator.CircleIndicator3
import java.util.concurrent.TimeUnit

class request_details : AppCompatActivity() {

    private var _binding: ActivityRequestDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        _binding = ActivityRequestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _binding?.backButton?.setOnClickListener { finish() }

        // استلام البيانات من Intent
        val productName = intent.getStringExtra("productName")
        val productId = intent.getStringExtra("productId") ?: "123"
        val requestedProduct = intent.getStringExtra("correspondingProduct")
        val description = intent.getStringExtra("description")
        val location = intent.getStringExtra("location")
        val category = intent.getStringExtra("category")
        val ownerId = intent.getStringExtra("ownerId") ?: "12345"
        val ownerName = intent.getStringExtra("ownerName")
        val requestId = intent.getStringExtra("requestId")
        val requesterId = intent.getStringExtra("requesterId") ?: "12345"
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
                val name = (doc?.getString("name") ?: "Sara").ifBlank { "Sara" }

                binding.userName.text = "$name"
                binding.userPhone.text = "$phone"
            }

        // تحويل مسارات الصور ل Bitmaps
        val imageBitmaps = imagesPaths.mapNotNull { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }

        val adapter = SelectedImagesPagerAdapter(imageBitmaps)
        binding.viewPagerImages.adapter = adapter

        val indicator: CircleIndicator3 = binding.indicator
        indicator.setViewPager(binding.viewPagerImages)
        loadOwnerData(requesterId)

        // ---------------- دالة موحدة للإشعارات ----------------
        fun sendNotification(userId: String, title: String, message: String, iconRes: Int, type: String) {
            // Firestore
            val notifData = hashMapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type, // type محدد حسب نوع الإشعار
                "createdAt" to Timestamp.now(),
                "seen" to false
            )
            db.collection("notifications").add(notifData)

            // إشعار محلي
            NotificationHelper.showNotification(
                this,
                title,
                message,
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                R.drawable.swapit
            )
        }
        // الاتصال والرسائل عبر واتساب
        fun openWhatsApp(userId: String) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                val phone = doc?.getString("phone") ?: ""
                if (phone.isNotEmpty()) {
                    try {
                        val url = "https://wa.me/$phone"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setPackage("com.whatsapp")
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

        binding.btnCall.setOnClickListener { openWhatsApp(ownerId) }
        binding.btnMessage.setOnClickListener { openWhatsApp(ownerId) }

        // قبول الطلب
        binding.btnAccept.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_accept_request, null)
            val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnConfirm.setOnClickListener {
                alertDialog.dismiss()
                requestId?.let { reqId ->
                    // قبول الطلب
                    db.collection("requests").document(reqId)
                        .update("state", RequestState.ACCEPTED)
                        .addOnSuccessListener {
                            // إشعار قبول للطلب
                            sendNotification(requesterId, "تم قبول طلبك!", "تم قبول طلبك سوف يتم حذف طلبك بعد 24 ساعة .", R.drawable.accept3, "accept3")

                            // رفض باقي الطلبات على نفس العرض
                            db.collection("requests").whereEqualTo("productId", productId)
                                .get().addOnSuccessListener { query ->
                                    for (doc in query.documents) {
                                        if (doc.id != reqId) {
                                            val otherRequesterId = doc.getString("requesterId") ?: continue
                                            doc.reference.update("state", RequestState.REJECTED.name)
                                            sendNotification(otherRequesterId, "تم رفض طلبك", "تم رفض طلبك  من قبل العارض  سيتم حذف طلبك عد 24 ساعة .", R.drawable.close, "close")

                                            // حذف الطلب بعد 24 ساعة (تجربة قصيرة 2 دقيقة)
                                            val data = Data.Builder().putString("requestId", doc.id).build()
                                            val work = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                                                .setInitialDelay(24, TimeUnit.HOURS)
                                                .setInputData(data)
                                                .build()
                                            WorkManager.getInstance(this).enqueue(work)
                                        }
                                    }
                                }

                            // حذف العرض
                            db.collection("offers").document(productId)
                                .delete()

                            // إشعار صاحب العرض
                            sendNotification(ownerId, "تم قبول أحد الطلبات على عرضك", "تم عملية التبادل بنجاح سيتم حذف عرضك عد 24 ساعة  .", R.drawable.accept3, "accept3")

                            // عرض رسالة نجاح
                            val successView = LayoutInflater.from(this).inflate(R.layout.dialog_success_toast, null)
                            val successDialog = AlertDialog.Builder(this).setView(successView).create()
                            successDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            successDialog.setCanceledOnTouchOutside(false)
                            successDialog.show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (successDialog.isShowing) successDialog.dismiss()
                                finish()
                            }, 3000)
                        }
                        .addOnFailureListener { e -> Toast.makeText(this, "خطأ عند تحديث الطلب: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }

            btnCancel.setOnClickListener { alertDialog.dismiss() }
            alertDialog.show()
        }

        // رفض الطلب
        binding.btnReject.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reject_request, null)
            val alertDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

            btnCancel.setOnClickListener { alertDialog.dismiss() }
            btnConfirm.setOnClickListener {
                alertDialog.dismiss()
                requestId?.let { docId ->
                    db.collection("requests").document(docId)
                        .update("state", RequestState.REJECTED.name)
                        .addOnSuccessListener {
                            // إشعار رفض للطلب
                            sendNotification(requesterId, "تم رفض طلبك", "تم رفض طلبك  من قبل العارض  سيتم حذف طلبك ", R.drawable.close, "close")
                            // حذف الطلب بعد 24 ساعة
                            Handler(Looper.getMainLooper()).postDelayed({
                                db.collection("requests").document(docId).delete()
                            }, 24 * 60 * 60 * 1000) // دقيقتين

                            Toast.makeText(this, "تم رفض الطلب", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
            }
            alertDialog.show()
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
