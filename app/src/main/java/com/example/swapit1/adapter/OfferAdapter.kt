package com.example.swapit1.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import com.example.swapit1.R
import com.example.swapit1.databinding.ItemCardMyOffersBinding
import com.example.swapit1.model.Offers
import com.example.swapit1.ui.SpecificProductRequests
import com.example.swapit1.ui.details.offer_details
import com.example.swapit1.edit.edit_offer
import com.example.swapit1.model.Request
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class OfferAdapter(
    private val context: Context,
    private val items: List<Offers>
) : ArrayAdapter<Offers>(context, 0, items) {
    private val firestore = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemCardMyOffersBinding
        val itemView: View

        if (convertView == null) {
            binding = ItemCardMyOffersBinding.inflate(LayoutInflater.from(context), parent, false)
            itemView = binding.root
            itemView.tag = binding
        } else {
            itemView = convertView
            binding = itemView.tag as ItemCardMyOffersBinding
        }

        val item = items[position]

        // عرض أول صورة (للقائمة فقط)
        if (item.images.isNotEmpty()) {
            val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.productImage.setImageBitmap(bitmap)
        } else {
            binding.productImage.setImageResource(R.drawable.flour10) // صورة افتراضية
        }

        binding.haveProductText.text = item.productName
        binding.wantProductText.text = "مقابل : ${item.requestedProduct}"

        // قائمة الخيارات: تعديل / حذف
        binding.menuButton.setOnClickListener {
            val imagePaths = arrayListOf<String>()
            item.images.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val tempFile = File(context.cacheDir, "offer_${item.offerId }_$i.jpg")
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    fos.close()
                    imagePaths.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val popup = PopupMenu(context, binding.menuButton)
            popup.menuInflater.inflate(R.menu.item_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        val intent = Intent(context, edit_offer::class.java)
                        intent.putExtra("offerId", item.offerId)
                        intent.putExtra("productName", item.productName)
                        intent.putExtra("requestedProduct", item.requestedProduct)
                        intent.putExtra("description", item.description)
                        intent.putExtra("location", item.location)
                        intent.putExtra("category", item.category)
                        intent.putStringArrayListExtra("imagesPaths", imagePaths)
                        context.startActivity(intent)
                        true
                    }
                    R.id.menu_delete -> {
                        val dialogView = LayoutInflater.from(context)
                            .inflate(R.layout.dialog_delete_offer, null)
                        val alertDialog = android.app.AlertDialog.Builder(context)
                            .setView(dialogView)
                            .setCancelable(false)
                            .create()

                        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
                        val btnDelete = dialogView.findViewById<android.widget.Button>(R.id.btnDelete)
                        val tvMessage = dialogView.findViewById<android.widget.TextView>(R.id.tvDeleteMessage)

                        tvMessage.text = "هل أنت متأكد من حذف عرضك ؟"
                        btnCancel.setOnClickListener { alertDialog.dismiss() }
                        btnDelete.setOnClickListener {
                            alertDialog.dismiss()
                            Toast.makeText(context, "تم حذف العرض: ${item.productName}", Toast.LENGTH_SHORT).show()
                            item.offerId ?.let { docId ->
                                FirebaseFirestore.getInstance().collection("offers")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener {
                                        deleteAllReqForOffer(docId)
                                        Toast.makeText(context, "تم حذف العرض: ${item.productName}", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "خطأ عند الحذف: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        alertDialog.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        binding.viewRequestsText.setOnClickListener {
            val intent = Intent(context, SpecificProductRequests::class.java)
            intent.putExtra("offerId", item.  offerId )
            intent.putExtra("productName", item.productName)
            intent.putExtra("requestedProduct", item.requestedProduct)
            intent.putExtra("description", item.description)
            intent.putExtra("location", item.location)
            intent.putExtra("category", item.category)
            intent.putExtra("postTimestampMillis", item.createdAt?.toDate()?.time ?: 0L)

            intent.putExtra("ownerId", item.ownerId)
           // intent.putStringArrayListExtra("imagesPaths", imagePaths) // الصور كملفات
            context.startActivity(intent)
        }

        // عند الضغط على العنصر: فتح تفاصيل العرض مع صور آمنة
        binding.root.setOnClickListener {

            // تحويل Base64 لكل الصور إلى ملفات مؤقتة
            val imagePaths = arrayListOf<String>()
            item.images.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val tempFile = File(context.cacheDir, "offer_${item.offerId }_$i.jpg")
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    fos.close()
                    imagePaths.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }



            val intent = Intent(context, offer_details::class.java)
            intent.putExtra("offerId", item.  offerId )
            intent.putExtra("productName", item.productName)
            intent.putExtra("requestedProduct", item.requestedProduct)
            intent.putExtra("description", item.description)
            intent.putExtra("location", item.location)
            intent.putExtra("category", item.category)
            intent.putExtra("postTimestampMillis", item.createdAt?.toDate()?.time ?: 0L)

            intent.putExtra("ownerId", item.ownerId)
            intent.putStringArrayListExtra("imagesPaths", imagePaths) // الصور كملفات

            context.startActivity(intent)
        }

        return itemView
    }

    private fun deleteAllReqForOffer(offerId: String) {
        firestore.collection("requests")
            .whereEqualTo("offerId", offerId) // جيب كل الطلبات المرتبطة بالعرض
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference) // احذف كل طلب
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "تم حذف جميع الطلبات المرتبطة بالعرض", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "خطأ عند حذف الطلبات: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "فشل جلب الطلبات: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
