package com.example.swapit1.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.swapit1.R
import com.example.swapit1.model.RequestState
import com.example.swapit1.model.requestItem
import android.graphics.drawable.GradientDrawable
import android.util.Base64
import android.widget.Button
import androidx.cardview.widget.CardView
import com.example.swapit1.edit.edit_request
import com.example.swapit1.model.Request
import com.example.swapit1.ui.details.My_Request_Details
import com.example.swapit1.ui.details.request_details


class RequestAdapter(
    private val context: Context,
    private val items: List<Request>
) : ArrayAdapter<Request>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_card_my_requests, parent, false)

        val item = items[position]

        val imageView = itemView.findViewById<ImageView>(R.id.productImage)
        val haveText = itemView.findViewById<TextView>(R.id.haveProduct)
        val wantText = itemView.findViewById<TextView>(R.id.wantProduct)
        val menuButton = itemView.findViewById<ImageButton>(R.id.menuButton)
        val state = itemView.findViewById<TextView>(R.id.currentState)
        val cardItem = itemView.findViewById<CardView>(R.id.MyCardRequest)


//        val radiusInPx = 20
//        Glide.with(context)
//            .load(item.images)
//            .into(imageView) // لا داعي لـ transform إذا بتستخدمي clipToOutline





        // عرض أول صورة (للقائمة فقط)
        if (item.images.isNotEmpty()) {
            val imageBytes = Base64.decode(item.images[0], Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.flour10) // صورة افتراضية
        }
        wantText.text = "${item.productName}"
        haveText.text = "مقابل : ${item.correspondingProduct}"

        val statusBackground = state.background as GradientDrawable

        when (item.state) {
            RequestState.PENDING -> {
                state.text = "قيد الانتظار"
                state.setTextColor(context.getColor(R.color.blue))
                statusBackground.setStroke(2, context.getColor(R.color.blue))  // تغيير لون الحدود
            }
            RequestState.ACCEPTED -> {
                state.text = "تم القبول"
                state.setTextColor(context.getColor(R.color.green))
                statusBackground.setStroke(2, context.getColor(R.color.green))
            }
            RequestState.REJECTED -> {
                state.text = "تم الرفض"
                state.setTextColor(context.getColor(R.color.red))
                statusBackground.setStroke(2, context.getColor(R.color.red))
            }
        }




        menuButton.setOnClickListener {
            val popup = PopupMenu(context, menuButton)
            popup.menuInflater.inflate(R.menu.item_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        val intent = Intent(context, edit_request ::class.java)
                        context.startActivity(intent)
                        true
                    }
                    R.id.menu_delete -> {
                        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_offer, null)
                        val alertDialog = android.app.AlertDialog.Builder(context)
                            .setView(dialogView)
                            .setCancelable(false)
                            .create()

                        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
                        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
                        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDeleteMessage)

                        tvMessage.text = "هل أنت متأكد من حذف طلبك ؟"
                        btnCancel.setOnClickListener {
                            alertDialog.dismiss()
                        }

                        btnDelete.setOnClickListener {
                            alertDialog.dismiss()
                            // TODO: احذفي العنصر من القائمة أو اعملي له حذف من السيرفر
                            Toast.makeText(context, "تم حذف العرض: ${item.productName}", Toast.LENGTH_SHORT).show()
                        }

                        alertDialog.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        cardItem.setOnClickListener {
            val intent = Intent(context, My_Request_Details::class.java)
            context.startActivity(intent)
        }


        return itemView
    }
}