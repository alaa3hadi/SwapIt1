package com.example.swapit1.ui.Category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapit1.adapter.CardAdapter
import com.example.swapit1.databinding.FragmentCategoryBinding
import com.example.swapit1.model.CardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val userId by lazy { auth.currentUser?.uid ?: "guest" }
    private val userName by lazy { auth.currentUser?.displayName ?: "guest_user" }
    private val cardAdapter by lazy { CardAdapter(requireActivity(), mutableListOf(), userId, userName) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)

        // SafeArgs
        val args = CategoryFragmentArgs.fromBundle(requireArguments())
        val categoryName = args.categoryName

        binding.textCategory.text = categoryName

        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = cardAdapter
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // شغّل الشيمر وحمّل البيانات
        showLoading(true)
        loadCategoryOffers(categoryName)

        return binding.root
    }

    private fun loadCategoryOffers(categoryName: String) {
        db.collection("offers")
            .whereEqualTo("category", categoryName)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                val b = _binding ?: return@addSnapshotListener

                if (e != null || snapshot == null) {
                    cardAdapter.submit(emptyList())
                    showLoading(false)
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { d -> docToCardItem(d) }
                cardAdapter.submit(list)
                showLoading(false)
            }
    }

    private fun docToCardItem(d: DocumentSnapshot): CardItem? {
        val images      = d.get("images") as? List<String> ?: emptyList()
        val name        = d.getString("productName") ?: "—"
        val request     = d.getString("requestedProduct") ?: "—"
        val loc         = d.getString("location") ?: "—"
        val ts          = d.getTimestamp("createdAt")
        val dec         = d.getString("description") ?: "—"
        val cat         = d.getString("category") ?: "—"
        val ownerId     = d.getString("ownerId") ?: "—"
        val ownerName   = d.getString("ownerName") ?: "—"
        val offerId     = d.id

        return CardItem(
            images           = images,
            productName      = name,
            requestedProduct = request,
            location         = loc,
            createdAt        = ts,
            description      = dec,
            category         = cat,
            ownerId          = ownerId,
            ownerName        = ownerName,
            offerId          = offerId
        )
    }

    /** تشغيل/إيقاف الشيمر وتبديل الظهور */
    private fun showLoading(loading: Boolean) {
        _binding?.let { b ->
            if (loading) {
                b.shimmerCategory.visibility = View.VISIBLE
                b.shimmerCategory.startShimmer()
                b.categoryRecyclerView.visibility = View.GONE
            } else {
                b.shimmerCategory.stopShimmer()
                b.shimmerCategory.visibility = View.GONE
                b.categoryRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
