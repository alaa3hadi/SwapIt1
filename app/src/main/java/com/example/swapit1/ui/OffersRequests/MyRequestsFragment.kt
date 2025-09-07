package com.example.swapit1.ui.OffersRequests

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.swapit1.R
import com.example.swapit1.adapter.OfferAdapter
import com.example.swapit1.adapter.RequestAdapter
import com.example.swapit1.model.Offers
import com.example.swapit1.model.Request
import com.example.swapit1.model.RequestState
import com.example.swapit1.model.offerItem
import com.example.swapit1.model.requestItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MyRequestsFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.cardListViewRequest)
        val emptyLayout = view.findViewById<View>(R.id.emptyLayoutReq)
        val addButton = view.findViewById<Button>(R.id.ButtonReq)

        val currentUserId = auth.currentUser?.uid ?: "user123"

        firestore.collection("requests")
            .whereEqualTo("requesterId", currentUserId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "خطأ عند جلب الطلبات: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {

                    val requests = snapshots.documents.mapNotNull { doc ->
                        val request = doc.toObject(Request::class.java)
                        request?.requestId  = doc.id // حفظ الـ documentId
                        request
                    }

                    Toast.makeText(requireContext(), "عدد العروض: ${requests.size}", Toast.LENGTH_SHORT).show()

                    listView.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE

                    // نرسل مباشرة قائمة Offers للـ Adapter
                    val adapter = RequestAdapter(requireContext(), requests)
                    listView.adapter = adapter

                } else {
                    listView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                }
            }

        addButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val listView = view.findViewById<ListView>(R.id.cardListViewRequest)
//
////        val items = listOf(
////            requestItem("2 كيلو سكر", "1 كيلو طحين", R.drawable.product1 , state = RequestState.PENDING ),
////            requestItem("سماعة بلوتوث", "شاحن سريع", R.drawable.prod3 , state = RequestState.ACCEPTED  ),
////            requestItem("كيلو طحين", "كيلو سكر", R.drawable.flour , state = RequestState.REJECTED  )
////
////        )
//
//
//
////        val emptyLayout = view.findViewById<View>(R.id.emptyLayoutReq)
////        val addButton = view.findViewById<Button>(R.id.ButtonReq)
////        val items = listOf<requestItem>() // ← أو أي مصدر بيانات عندك
////
////        if (items.isEmpty()) {
////            listView.visibility = View.GONE
////            emptyLayout.visibility = View.VISIBLE
////        } else {
////            listView.visibility = View.VISIBLE
////            emptyLayout.visibility = View.GONE
////            val adapter = RequestAdapter(requireContext(), items)
////            listView.adapter = adapter
////        }
////
////        addButton.setOnClickListener {
////            // الانتقال إلى شاشة إضافة عرض
////            // startActivity(Intent(context, AddOfferActivity::class.java))
////        }
//
////        val adapter = RequestAdapter(requireContext(), items)
////        listView.adapter = adapter
//
//    }
}