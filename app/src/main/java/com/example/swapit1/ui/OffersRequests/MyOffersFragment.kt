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
import com.example.swapit1.adapter.OfferAdapter
import com.example.swapit1.model.Offers
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyOffersFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_my_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        listView = view.findViewById(R.id.cardListView)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        addButton = view.findViewById(R.id.addOfferButton)

        val currentUserId = auth.currentUser ?.uid ?: "user123"

        // ابدأ الشيمر واظهره، اخفي القائمة والفراغ
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        listView.visibility = View.GONE
        emptyLayout.visibility = View.GONE

        firestore.collection("offers")
            .whereEqualTo("ownerId", currentUserId)
            .addSnapshotListener { snapshots, e ->
                // أوقف الشيمر مهما كانت النتيجة
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE

                if (e != null) {
                    Toast.makeText(requireContext(), "خطأ عند جلب العروض: ${e.message}", Toast.LENGTH_SHORT).show()
                    // عرض الفراغ مع زر الإضافة
                    listView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val offers = snapshots.documents.mapNotNull { doc ->
                        val offer = doc.toObject(Offers::class.java)
                        offer?.offerId = doc.id // حفظ الـ documentId
                        offer
                    }

                    listView.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE

                    val adapter = OfferAdapter(requireContext(), offers)
                    listView.adapter = adapter

                } else {
                    listView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                }
            }

        addButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_addOffer)
        }
    }
}
