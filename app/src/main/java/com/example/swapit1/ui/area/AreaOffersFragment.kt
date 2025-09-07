package com.example.swapit1.ui.area

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapit1.adapter.CardAdapter
import com.example.swapit1.databinding.FragmentAreaOffersBinding
import com.example.swapit1.model.CardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class AreaOffersFragment : Fragment() {

    private var _binding: FragmentAreaOffersBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    // User
    private var userId: String = "guest"
    private var userName: String = "guest_user"

    // UI
    private var cardAdapter: CardAdapter? = null
    private var areaArg: String = ""

    // Firestore listener
    private var offersReg: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAreaOffersBinding.inflate(inflater, container, false)

        // Args
        val args = AreaOffersFragmentArgs.fromBundle(requireArguments())
        areaArg = args.areaName ?: ""

        // Firebase
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // User
        auth?.currentUser?.let {
            userId = it.uid
            userName = it.displayName ?: "user"
        }

        // Title + Back
        binding.textCategory.text = if (areaArg.isNotBlank()) "عروض $areaArg" else "كل العروض"
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Recycler
        cardAdapter = CardAdapter(requireActivity(), mutableListOf(), userId, userName)
        binding.rvAreaOffers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAreaOffers.adapter = cardAdapter

        // شيمر
        showLoading(true)

        // استماع حي بدون get()
        listenAllForArea(areaArg)

        return binding.root
    }

    /**
     * استماع حيّ لكل العروض (بدون get):
     * - بنسمع لأحدث 200 عنصر مرتبة بـ createdAt
     * - لو في area بنفلتر محليًا (نتجنّب whereEqualTo+orderBy والفهرس المركّب)
     */
    private fun listenAllForArea(area: String) {
        // أوقف أي استماع سابق
        offersReg?.remove()

        val ref = db?.collection("offers") ?: return

        // استماع لأحدث العناصر
        offersReg = ref
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener

                if (e != null || snapshot == null) {
                    cardAdapter?.submit(emptyList())
                    showLoading(false)
                    return@addSnapshotListener
                }

                val docs = if (area.isBlank()) {
                    snapshot.documents
                } else {
                    snapshot.documents.filter { d ->
                        val loc = d.getString("location") ?: ""
                        loc == area || loc.startsWith("$area ")
                    }
                }

                val items = ArrayList<CardItem>()
                for (d in docs) {
                    toCard(d)?.let { items.add(it) }
                }

                cardAdapter?.submit(items)
                showLoading(false)
            }
    }

    private fun toCard(d: DocumentSnapshot): CardItem? {
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

    private fun showLoading(loading: Boolean) {
        if (_binding == null) return
        if (loading) {
            binding.shimmerAreaAll.visibility = View.VISIBLE
            binding.shimmerAreaAll.startShimmer()
            binding.rvAreaOffers.visibility = View.GONE
        } else {
            binding.shimmerAreaAll.stopShimmer()
            binding.shimmerAreaAll.visibility = View.GONE
            binding.rvAreaOffers.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // أوقف الاستماع الحي
        offersReg?.remove()
        offersReg = null

        _binding = null
        auth = null
        db = null
        cardAdapter = null
    }
}
