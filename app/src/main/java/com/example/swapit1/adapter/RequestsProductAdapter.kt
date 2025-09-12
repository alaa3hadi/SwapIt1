package com.example.swapit1.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.fitCenter
import com.example.swapit1.R
import com.example.swapit1.model.Request
import com.example.swapit1.model.RequestState
import com.example.swapit1.model.requestItem
import com.example.swapit1.model.requestsProductItem
import com.example.swapit1.ui.SpecificProductRequests
import com.example.swapit1.ui.details.request_details
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class RequestsProductAdapter (
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




//        Glide.with(context)
//            .load(item.images)
//
//            .transform(CenterCrop(), RoundedCorners(35))
//            .into(imageProduct)


        // عرض أول صورة (للقائمة فقط)
        if (item.images.isNotEmpty()) {
            val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageProduct.setImageBitmap(bitmap)
        } else {
            imageProduct.setImageResource(R.drawable.flour10) // صورة افتراضية
        }
        imageUser.setImageResource(R.drawable.profile)
        userName.text = "${item.requesterName}"
        wantProduct.text = " لديه : ${item.productName}"
        location.text = "${item.location}"


        val  acceptButton = itemView.findViewById<MaterialButton>(R.id.button_accept)  // زر قبول الطلب

        acceptButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_accept_request, null)

            val alertDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

// الوصول للأزرار داخل الـ dialog
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnConfirm.setOnClickListener {
                alertDialog.dismiss()

                val successView = LayoutInflater.from(context).inflate(R.layout.dialog_success_toast, null)

                val successDialog = AlertDialog.Builder(context)
                    .setView(successView)
                    .create()

// جعل الخلفية شفافة
                successDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

// منع الإغلاق عند الضغط خارج الحوار (اختياري)
                successDialog.setCanceledOnTouchOutside(false)

// إظهار الخلفية مغبشة باستخدام theme مخصص
                successDialog.window?.setDimAmount(0.6f)  // 0.0 للشفافية الكاملة، 1.0 للغباشة الكاملة
                successDialog.setOnShowListener {
                    val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
                    successDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                successDialog.show()


// إغلاق بعد 3 ثواني
                Handler(Looper.getMainLooper()).postDelayed({
                    if (successDialog.isShowing) {
                        successDialog.dismiss()
                        //إشعار عند مرسل الطلب بقبول طلبه


                    }
                }, 3000)

            }

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()

        }

        cardItem.setOnClickListener {
            // تحويل Base64 لكل الصور إلى ملفات مؤقتة
            val imagePaths = arrayListOf<String>()
            item.images.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val tempFile = File(context.cacheDir, "request_${item.requestId }_$i.jpg")
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    fos.close()
                    imagePaths.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            val intent = Intent(context, request_details::class.java)
            intent.putExtra("requestId", item.requestId )
            intent.putExtra("productName", item.productName)
            intent.putExtra("ownerName", item.ownerName)
            intent.putExtra("correspondingProduct", item.correspondingProduct)
            intent.putExtra("description", item.description)
            intent.putExtra("location", item.location)
            intent.putExtra("category", item.category)
            intent.putExtra("postTimestampMillis", item.createdAt?.toDate()?.time ?: 0L)
            intent.putExtra("productId", item.productId)
            intent.putExtra("ownerId", item.ownerId)
            intent.putStringArrayListExtra("imagesPaths", imagePaths) // الصور كملفات
            intent.putExtra("requesterId" , item.requesterId)
            intent.putExtra("requesterName" , item.requesterName)
            intent.putExtra("State" , item.state)

            context.startActivity(intent)
        }
        buttonCall.setOnClickListener {
            firestore.collection("users").document(item.ownerId)
                .addSnapshotListener { doc, _ ->
                    val phone = (doc?.getString("phone") ?: "1234").ifBlank { "1234" }
                    if (!phone.isNullOrEmpty()) {
                        try {
                            val url = "https://wa.me/$phone"  // صيغة الواتساب الرسمية
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setPackage("com.whatsapp") // يفتح الواتس فقط
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



        return itemView
    }
}