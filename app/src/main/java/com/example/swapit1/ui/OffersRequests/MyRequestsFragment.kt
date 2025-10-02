package com.example.swapit1.ui.OffersRequests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.swapit1.R
import com.example.swapit1.adapter.RequestAdapter
import com.example.swapit1.model.Request
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyRequestsFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var listView: ListView
    private lateinit var emptyLayout: View
    private lateinit var addButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shimmerLayout = view.findViewById(R.id.shimmerLayoutReq)
        listView = view.findViewById(R.id.cardListViewRequest)
        emptyLayout = view.findViewById(R.id.emptyLayoutReq)
        addButton = view.findViewById(R.id.ButtonReq)

        val currentUserId = auth.currentUser ?.uid ?: "user123"

        // ابدأ الشيمر واظهره، اخفي القائمة والفراغ
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        listView.visibility = View.GONE
        emptyLayout.visibility = View.GONE

        firestore.collection("requests")
            .whereEqualTo("requesterId", currentUserId)
            .addSnapshotListener { snapshots, e ->
                // أوقف الشيمر مهما كانت النتيجة
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE

                if (e != null) {
                    Toast.makeText(requireContext(), "خطأ عند جلب الطلبات: ${e.message}", Toast.LENGTH_SHORT).show()
                    // عرض الفراغ مع زر التصفح
                    listView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val requests = snapshots.documents.mapNotNull { doc ->
                        val request = doc.toObject(Request::class.java)
                        request?.requestId = doc.id // حفظ الـ documentId
                        request
                    }

                    listView.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE

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
}