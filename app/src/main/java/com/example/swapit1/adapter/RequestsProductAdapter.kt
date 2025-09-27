package com.example.swapit1.adapter

import DeleteRequestWorker
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import com.example.swapit1.NotificationHelper
import com.example.swapit1.R
import com.example.swapit1.model.Request
import com.example.swapit1.model.RequestState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class RequestsProductAdapter(
    private val context: Context,
    private val items: List<Request>
) : ArrayAdapter<Request>(context, 0, items) {

    private val firestore = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_card_requests_product_specific, parent, false)

        val item = items[position]

        val imageUser = itemView.findViewById<ImageView>(R.id.userImage)
        val userName = itemView.findViewById<TextView>(R.id.fromUser)
        val imageProduct = itemView.findViewById<ImageView>(R.id.productImage)
        val wantProduct = itemView.findViewById<TextView>(R.id.wantProduct)
        val location = itemView.findViewById<TextView>(R.id.locationUser)
        val cardItem = itemView.findViewById<CardView>(R.id.cardRequestProductSpecific)
        val buttonCall = itemView.findViewById<Button>(R.id.button_call)
        val acceptButton = itemView.findViewById<MaterialButton>(R.id.button_accept)

        // عرض أول صورة للمنتج
        if (item.images.isNotEmpty()) {
            val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageProduct.setImageBitmap(bitmap)
        } else {
            imageProduct.setImageResource(R.drawable.flour10)
        }

        imageUser.setImageResource(R.drawable.profile)
        userName.text = item.requesterName
        wantProduct.text = " لديه : ${item.productName}"
        location.text = item.location

        // عند الضغط على قبول الطلب
        acceptButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_accept_request, null)
            val alertDialog = AlertDialog.Builder(context).setView(dialogView).create()
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnConfirm.setOnClickListener {
                alertDialog.dismiss()
                acceptRequest(item)
            }

            btnCancel.setOnClickListener { alertDialog.dismiss() }
            alertDialog.show()
        }

        // الاتصال عبر واتساب
        buttonCall.setOnClickListener {
            firestore.collection("users").document(item.ownerId)
                .addSnapshotListener { doc, _ ->
                    val phone = doc?.getString("phone") ?: ""
                    if (phone.isNotEmpty()) {
                        try {
                            val url = "https://wa.me/$phone"
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setPackage("com.whatsapp")
                            intent.data = android.net.Uri.parse(url)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "الواتساب غير مثبت على هذا الجهاز", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // عند الضغط على الكارد لفتح التفاصيل
        cardItem.setOnClickListener {
            // تحويل Base64 لكل الصور إلى ملفات مؤقتة
            val imagePaths = arrayListOf<String>()
            item.images.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val tempFile = File(context.cacheDir, "request_${item.requestId}_$i.jpg")
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    fos.close()
                    imagePaths.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val intent = Intent(context, com.example.swapit1.ui.details.request_details::class.java)
            intent.putExtra("requestId", item.requestId)
            intent.putExtra("productName", item.productName)
            intent.putExtra("ownerName", item.ownerName)
            intent.putExtra("correspondingProduct", item.correspondingProduct)
            intent.putExtra("description", item.description)
            intent.putExtra("location", item.location)
            intent.putExtra("category", item.category)
            intent.putExtra("postTimestampMillis", item.createdAt?.toDate()?.time ?: 0L)
            intent.putExtra("productId", item.productId)
            intent.putExtra("ownerId", item.ownerId)
            intent.putStringArrayListExtra("imagesPaths", imagePaths)
            intent.putExtra("requesterId", item.requesterId)
            intent.putExtra("requesterName", item.requesterName)
            intent.putExtra("State", item.state)

            context.startActivity(intent)
        }

        return itemView
    }

    private fun acceptRequest(item: Request) {
        val db = FirebaseFirestore.getInstance()
        val ownerId = item.ownerId
        val requesterId = item.requesterId
        val requestId = item.requestId
        val productId = item.productId

        // تحديث الطلب الحالي
        db.collection("requests").document(requestId)
            .update("state", RequestState.ACCEPTED.name)
            .addOnSuccessListener {
                // إشعار للطالب
                sendNotification(requesterId, "تم قبول طلبك!", "تم قبول طلبك سيتم حذف طلبك بعد 24 ساعة.", R.drawable.accept3, "accept3")

                // رفض باقي الطلبات على نفس العرض
                db.collection("requests").whereEqualTo("productId", productId)
                    .get().addOnSuccessListener { query ->
                        for (doc in query.documents) {
                            if (doc.id != requestId) {
                                val otherRequesterId = doc.getString("requesterId") ?: continue
                                doc.reference.update("state", RequestState.REJECTED.name)
                                sendNotification(otherRequesterId, "تم رفض طلبك", "تم رفض طلبك من قبل العارض سيتم حذف طلبك بعد 24 ساعة.", R.drawable.close, "close")

                                // حذف الطلب بعد دقيقتين
                                val data = Data.Builder().putString("requestId", doc.id).build()
                                val work = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                                    .setInitialDelay(24, TimeUnit.HOURS)
                                    .setInputData(data)
                                    .build()
                                WorkManager.getInstance(context).enqueue(work)
                            }
                        }
                    }

                // حذف العرض
                db.collection("offers").document(productId)
                    .delete()
                    .addOnSuccessListener {

                    }
                // جدولة حذف الطلب المقبول بعد 24 ساعة
                val acceptedData = Data.Builder().putString("requestId", requestId).build()
                val acceptedWork = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                    .setInitialDelay(24, TimeUnit.HOURS)
                    .setInputData(acceptedData)
                    .build()
                WorkManager.getInstance(context).enqueue(acceptedWork)

                // جدولة حذف العرض بعد 24 ساعة
                val offerData = Data.Builder().putString("offerId", productId).build()
                val offerWork = OneTimeWorkRequestBuilder<DeleteRequestWorker>()
                    .setInitialDelay(24, TimeUnit.HOURS)
                    .setInputData(offerData)
                    .build()
                WorkManager.getInstance(context).enqueue(offerWork)
                // إشعار صاحب العرض
                sendNotification(ownerId, "تم قبول أحد الطلبات على عرضك", "تم عملية التبادل بنجاح وسيتم حذف عرضك.", R.drawable.accept3, "accept3")

                // عرض رسالة نجاح
                val successView = LayoutInflater.from(context).inflate(R.layout.dialog_success_toast, null)
                val successDialog = AlertDialog.Builder(context).setView(successView).create()
                successDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                successDialog.setCanceledOnTouchOutside(false)
                successDialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (successDialog.isShowing) successDialog.dismiss()
                }, 3000)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "خطأ عند تحديث الطلب: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotification(userId: String, title: String, message: String, iconRes: Int, type: String) {
        val db = FirebaseFirestore.getInstance()
        val notifData = hashMapOf(
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "createdAt" to Timestamp.now(),
            "seen" to false
        )
        db.collection("notifications").add(notifData)

        // إشعار محلي
        NotificationHelper.showNotification(
            context,
            title,
            message,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            R.drawable.swapit
        )
    }
}
