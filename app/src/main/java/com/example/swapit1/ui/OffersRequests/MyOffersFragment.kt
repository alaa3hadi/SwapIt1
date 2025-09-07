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
import com.example.swapit1.model.Offers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyOffersFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.cardListView)
        val emptyLayout = view.findViewById<View>(R.id.emptyLayout)
        val addButton = view.findViewById<Button>(R.id.addOfferButton)

        val currentUserId = auth.currentUser?.uid ?: "user123"

        firestore.collection("offers")
            .whereEqualTo("ownerId", currentUserId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "خطأ عند جلب العروض: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {

                    val offers = snapshots.documents.mapNotNull { doc ->
                        val offer = doc.toObject(Offers::class.java)
                        offer?.offerId  = doc.id // حفظ الـ documentId
                        offer
                    }

                    Toast.makeText(requireContext(), "عدد العروض: ${offers.size}", Toast.LENGTH_SHORT).show()

                    listView.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE

                    // نرسل مباشرة قائمة Offers للـ Adapter
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




//        val emptyLayout = view.findViewById<View>(R.id.emptyLayout)
//        val addButton = view.findViewById<Button>(R.id.addOfferButton)
//        val items = listOf<offerItem>() // ← أو أي مصدر بيانات عندك
//
//        if (items.isEmpty()) {
//            listView.visibility = View.GONE
//            emptyLayout.visibility = View.VISIBLE
//        } else {
//            listView.visibility = View.VISIBLE
//            emptyLayout.visibility = View.GONE
//            val adapter = OfferAdapter(requireContext(), items)
//            listView.adapter = adapter
//        }
//
//        addButton.setOnClickListener {
//            // الانتقال إلى شاشة إضافة عرض
//            // startActivity(Intent(context, AddOfferActivity::class.java))
//        }