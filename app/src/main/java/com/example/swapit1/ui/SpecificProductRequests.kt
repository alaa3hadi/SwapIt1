package com.example.swapit1.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.swapit1.R
import com.example.swapit1.adapter.RequestsProductAdapter
import com.example.swapit1.model.Request
import com.example.swapit1.model.requestsProductItem
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore


class SpecificProductRequests : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.requests_product_specific)
        listView = findViewById(R.id.cardListViewRequest2)

//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        val titleTextView = toolbar.findViewById<TextView>(R.id.customToolbarTitle)
//        val backButton = toolbar.findViewById<ImageButton>(R.id.backButton)
//        titleTextView.text = "الطلبات"
//        setSupportActionBar(toolbar)
//
//        supportActionBar?.setDisplayShowTitleEnabled(false)
//        backButton.visibility = View.VISIBLE
//        backButton.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }





//        // 2. بيانات تجريبية (مثال، استبدليها ببياناتك الحقيقية)
//        val demoList = listOf(
//            requestsProductItem(
//                imageProduct = R.drawable.flour3,
//                imageUser = R.drawable.profile,
//                fromUser = " رضا محمد",
//                wantProduct = "2 كيلو طحين أسمر أو كيلو سكر",
//                location = "غزة - الرمال"
//            ),
//            requestsProductItem(
//                imageProduct = R.drawable.flour,
//                imageUser = R.drawable.profile,
//                fromUser = "ليلى أبو كويك",
//                wantProduct = "كيلو طحين ابيض",
//                location = "خان يونس"
//            )
//            ,
//            requestsProductItem(
//                imageProduct = R.drawable.flour1,
//                imageUser = R.drawable.profile,
//                fromUser = "مريم عصر",
//                wantProduct = "كيلو طحين أونروا أبيض ",
//                location = "غزة-تل الهوا"
//            ),
//            requestsProductItem(
//                imageProduct = R.drawable.flour,
//                imageUser = R.drawable.profile,
//                fromUser = "رامي جبر",
//                wantProduct = "كيلو طحين أسمر لكن نظيف",
//                location = "غزة - التفاح"
//            ),
//            requestsProductItem(
//                imageProduct = R.drawable.flour10,
//                imageUser = R.drawable.profile,
//                fromUser = "محمد ",
//                wantProduct = "1 كيلو طحين",
//                location = "غزة- النصر"
//            )
//        )

        val offerId = intent.getStringExtra("offerId") ?: return
        Log.e("offerId" ,"offerId = ${offerId}")

        // الآن اعرض الطلبات المرتبطة بهذا العرض
        loadRequestsForOffer(offerId)


        // 3. ربط الأدابتر مع الليست




    }

    private fun loadRequestsForOffer(offerId: String) {
        firestore.collection("requests")
            .whereEqualTo("productId", offerId)   // كل الطلبات لهذا العرض
            .get()
            .addOnSuccessListener { result ->
                val requests = result.toObjects(Request::class.java)
                // اربطهم مع RecyclerView/Adapter
                val adapter = RequestsProductAdapter(this, requests)
                listView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "فشل تحميل الطلبات: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}