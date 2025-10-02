// app/src/main/java/com/example/swapit1/ui/home/HomeFragment.kt
package com.example.swapit1.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapit1.R
import com.example.swapit1.adapter.AreaCardAdapter
import com.example.swapit1.adapter.CardAdapter
import com.example.swapit1.adapter.CategoryAdapter
import com.example.swapit1.adapter.SearchCardAdapter
import com.example.swapit1.databinding.FragmentHomeBinding
import com.example.swapit1.model.AreaItem
import com.example.swapit1.model.CardItem
import com.example.swapit1.model.CategoryItem
import com.example.swapit1.model.Search
import com.example.swapit1.ui.myAccount.privacy_policy
import com.example.swapit1.ui.notifications.NotificationsFragment
import com.example.swapit1.ui.search.FilterBottomSheet
import com.example.swapit1.ui.search.FilterOptions
import com.example.swapit1.ui.search.SortOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Firebase (بدون lazy)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // Listener للإشعارات غير المقروءة
    private var notificationsListener: ListenerRegistration? = null
    // User
    private var userId: String = "guest"
    private var userName: String = "guest_user"
    private var userArea: String? = null

    // Adapters
    private lateinit var areaAdapter: AreaCardAdapter
    private lateinit var allAdapter: CardAdapter
    private lateinit var searchAdapter: SearchCardAdapter

    // Filters
    private var currentFilters = FilterOptions()
    private val locationsList  = listOf("شمال غزة", "غزة", "الوسطى", "خانيونس", "رفح")
    private val categoriesList = listOf("اطفال", "اثاث", "طعام", "ملابس", "الكترونيات","اخرى")
    private val SEARCH_FIELD = "productName"

    // ثابتة للتصنيفات
    private val categoryList = listOf(
        CategoryItem("طعام", R.drawable.food4),
        CategoryItem("ملابس", R.drawable.clothing4),
        CategoryItem("اطفال", R.drawable.baby4),
        CategoryItem("إلكترونيات", R.drawable.tv4),
        CategoryItem("أثاث", R.drawable.fur4),
        CategoryItem("اخرى", R.drawable.other4)
    )

    // Firestore listeners لإيقافها عند مغادرة الشاشة
    private var areaReg: ListenerRegistration? = null
    private var allReg: ListenerRegistration? = null
    private var searchReg: ListenerRegistration? = null
    private var userDocReg: ListenerRegistration? = null

    private var searchDebounce: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Firebase
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // User
        auth.currentUser?.let {
            userId = it.uid
            userName = it.displayName ?: "user"
        }

        // تهيئة الأدابترات
        areaAdapter  = AreaCardAdapter(userId, userName ,requireActivity(), mutableListOf())
        allAdapter   = CardAdapter(requireActivity(), mutableListOf(), userId, userName)
        searchAdapter = SearchCardAdapter(
            activity = requireActivity(),
            items = mutableListOf(),
            requesterId = userId,
            requesterName = userName
        )

        binding.root.setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val pressedOutsideSearchRow = !isTouchInside(binding.searchRow, ev)
                val searchOpen = binding.searchResultsRecyclerView.visibility == View.VISIBLE
                if (pressedOutsideSearchRow && searchOpen) {
                    closeSearchUI()
                }
            }
            // ارجع false عشان ما تمنع الضغطات الطبيعية (أزرار/قوائم...)
            false
        }
        binding.searchBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.searchResultsRecyclerView.visibility == View.VISIBLE) {
                closeSearchUI()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val searchOpen = binding.searchResultsRecyclerView.visibility == View.VISIBLE
            if (searchOpen) {
                closeSearchUI()
            } else {
                // خلّي النظام يتصرف طبيعي
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }


        // التصنيفات
        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        binding.categoryRecyclerView.adapter = CategoryAdapter(categoryList) { c ->
            val action = HomeFragmentDirections.actionNavigationHomeToCategoryFragment(c.name)
            findNavController().navigate(action)
        }

        // جديد في منطقتك (10 عناصر)
        binding.newItemsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.newItemsRecyclerView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        binding.newItemsRecyclerView.adapter = areaAdapter

        // كل العروض (خلّيه يسكّرول بنفسه لو تقدر تغيّري الواجهة. إن بقي داخل NestedScrollView تجنّبي قوائم ضخمة)
        binding.cardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cardRecyclerView.setHasFixedSize(true)
        binding.cardRecyclerView.adapter = allAdapter

        // نتائج البحث
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecyclerView.adapter = searchAdapter

        // زر "عرض الكل" + خط
        binding.btnAreaViewAll.paint.isUnderlineText = true
        binding.btnAreaViewAll.setOnClickListener {
            val areaArg = userArea ?: ""
            val action = HomeFragmentDirections.actionNavigationHomeToAreaOffersFragment(areaArg)
            findNavController().navigate(action)
        }


        //gghkjrhg gghauhvhj

        binding.notificationIcon.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }
        // فلتر
        binding.btnFilter.setOnClickListener {
            FilterBottomSheet(currentFilters, locationsList, categoriesList) { f ->
                currentFilters = f
                val text = binding.searchBox.text?.toString() ?: ""
                runSearchLive(currentFilters, text)
            }.show(childFragmentManager, "filters")
        }

        // بحث حي + ديباونس بسيط 300ms
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchDebounce?.let { binding.searchBox.removeCallbacks(it) }
                searchDebounce = Runnable { runSearchLive(currentFilters, s?.toString() ?: "") }
                binding.searchBox.postDelayed(searchDebounce!!, 300)
            }
        })

        // شيمر أولي
        showAreaLoading(true)
        showAllLoading(true)
        updateSectionVisibility(false)

        // تحميل حي لاسم/منطقة المستخدم ثم خلاصات حيّة
        listenUserAndFeeds()

        return binding.
        root
    }

    /** استماع لدوكيومنت المستخدم (لو مسجل) */
    private fun listenUserAndFeeds() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.userName.text = "مرحباً بك"
            userArea = null
            listenAreaFeed(null)
            listenAllFeed()
            return
        }

        // استماع حي لبيانات المستخدم (لو بدّك مرة واحدة فقط استخدمي get، لكن انتِ طلبتي بدون get)
        userDocReg?.remove()
        userDocReg = db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (_binding == null) return@addSnapshotListener
                val name = (doc?.getString("name") ?: "مستخدم").ifBlank { "مستخدم" }
                binding.userName.text = "مرحباً بك، $name"
                val loc = doc?.getString("location")
                userArea = if (loc.isNullOrBlank()) null else loc
                //➤ تحميل صورة المستخدم من Base64 إذا موجودة
                val photoBase64 = doc?.getString("photoBase64")
                if (!photoBase64.isNullOrEmpty()) {
                    try {
                        val bytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.userImage.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        binding.userImage.setImageResource(R.drawable.user_icon) // افتراضي لو فشل التحويل
                    }
                } else {
                    binding.userImage.setImageResource(R.drawable.user_icon) // صورة افتراضية
                }

                // كل مرة تتغير منطقة المستخدم نعيد الاستماع للقسم
                listenAreaFeed(userArea)
                // “كل العروض” حيّ
                listenAllFeed()
            }
    }

    /** استماع حي لقسم “جديد في منطقتك” (10 عناصر) */
    private fun listenAreaFeed(area: String?) {
        areaReg?.remove()

        val base = db.collection("offers")
        val q = if (area.isNullOrBlank()) {
            base.orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            base.whereEqualTo("location", area)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }.limit(10)

        areaReg = q.addSnapshotListener { snap, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null || snap == null) {
                areaAdapter.submit(emptyList())
                showAreaLoading(false)
                return@addSnapshotListener
            }
            val list = ArrayList<AreaItem>()
            for (d in snap.documents) {
                val imgs = d.get("images") as? List<String>
                list.add(
                    AreaItem(
                        location = area ?: (d.getString("location") ?: "—"),
                        images = listOfNotNull(imgs?.firstOrNull()),
                        productName = d.getString("productName") ?: "—",
                        requestedProduct = d.getString("requestedProduct") ?: "—",
                        createdAt = d.getTimestamp("createdAt"),
                        ownerId = d.getString("ownerId") ?: "—",
                        ownerName = d.getString("ownerName") ?: "—"
                    )
                )
            }
            areaAdapter.submit(list)
            showAreaLoading(false)
        }
    }

    /** استماع حي لقسم “كل العروض” */
    private fun listenAllFeed() {
        allReg?.remove()

        allReg = db.collection("offers")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null || snap == null) {
                    allAdapter.submit(emptyList())
                    showAllLoading(false)
                    return@addSnapshotListener
                }
                val list = ArrayList<CardItem>()
                for (d in snap.documents) {
                    toCardItem(d)?.let { list.add(it) }
                }
                allAdapter.submit(list)
                showAllLoading(false)
            }
    }

    /** بحث + فلاتر “لايف” (بدون get) */
    private fun runSearchLive(filters: FilterOptions, raw: String) {
        val text = raw.trim()
        val wantSearch = text.isNotEmpty()
        !filters.location.isNullOrBlank()
        !filters.category.isNullOrBlank() || filters.sort != SortOption.NEWEST
        updateSectionVisibility(wantSearch)

        // أوقف أي Listener قديم للبحث
        searchReg?.remove()
        if (!wantSearch) {
            searchAdapter.submit(emptyList())
            return
        }

        val col = db.collection("offers")

        // في حالة نص للبحث: نستخدم orderBy(productName) + range على نفس الحقل (لا يحتاج فهرس مركّب)
        if (text.isNotEmpty()) {
            var q: Query = col
                .orderBy(SEARCH_FIELD)
                .whereGreaterThanOrEqualTo(SEARCH_FIELD, text)
                .whereLessThanOrEqualTo(SEARCH_FIELD, text + "\uf8ff")
                .limit(50)

            // نترك الفلاتر (location/category) “محلية” لتجنب فهارس إضافية
            searchReg = q.addSnapshotListener { snap, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null || snap == null) {
                    searchAdapter.submit(emptyList())
                    return@addSnapshotListener
                }
                var list = ArrayList<Search>()
                for (d in snap.documents) {
                    val s = d.toObject(Search::class.java)
                    if (s != null) { s.id = d.id; list.add(s) }
                }
                // فلترة محلية
                if (!filters.location.isNullOrBlank()) {
                    list = ArrayList(list.filter {
                        val loc = it.location ?: ""
                        (loc == filters.location) || loc.startsWith(filters.location + " ")
                    })
                }
                if (!filters.category.isNullOrBlank()) {
                    list = ArrayList(list.filter { it.category == filters.category })
                }
                // ترتيب محلي
                val sorted = if (filters.sort == SortOption.NEWEST) {
                    list.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                } else {
                    list.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
                }
                searchAdapter.submit(sorted)
            }
            return
        }

        // بدون نص: حاول نطبق الفلاتر على السيرفر (قد يتطلب فهرس مركّب)
        var q: Query = col
        if (!filters.location.isNullOrBlank()) q = q.whereEqualTo("location", filters.location)
        if (!filters.category.isNullOrBlank()) q = q.whereEqualTo("category", filters.category)

        q = if (filters.sort == SortOption.NEWEST) {
            q.orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            q.orderBy("createdAt", Query.Direction.ASCENDING)
        }
        q = q.limit(50)

        searchReg = q.addSnapshotListener { snap, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null || snap == null) {
                // لو فشل بسبب فهرس، Firestore رح يعطيك لينك—اعمليه من الـConsole
                searchAdapter.submit(emptyList())
                return@addSnapshotListener
            }
            val list = ArrayList<Search>()
            for (d in snap.documents) {
                val s = d.toObject(Search::class.java)
                if (s != null) { s.id = d.id; list.add(s) }
            }
            searchAdapter.submit(list)
        }
    }

    // تحويل Document → CardItem
    private fun toCardItem(d: DocumentSnapshot): CardItem? {
        val images   = d.get("images") as? List<String> ?: emptyList()
        val name     = d.getString("productName") ?: "—"
        val req      = d.getString("requestedProduct") ?: "—"
        val loc      = d.getString("location") ?: "—"
        val ts       = d.getTimestamp("createdAt")
        val dec      = d.getString("description") ?: "—"
        val cat      = d.getString("category") ?: "—"
        val ownerId  = d.getString("ownerId") ?: "—"
        val ownerName= d.getString("ownerName") ?: "—"
        val id       = d.id

        return CardItem(
            images = images,
            productName = name,
            requestedProduct = req,
            location = loc,
            createdAt = ts,
            description = dec,
            category = cat,
            ownerId = ownerId,
            ownerName = ownerName,
            offerId = id
        )
    }
    private fun isTouchInside(view: View, ev: MotionEvent): Boolean {
        val r = Rect()
        view.getGlobalVisibleRect(r)
        return r.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        view?.windowToken?.let { imm?.hideSoftInputFromWindow(it, 0) }
    }

    private fun closeSearchUI() {
        hideKeyboard()
        binding.searchBox.clearFocus()
        // اختياري: امسح النص
        // binding.searchBox.setText("")
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.homeContent.visibility = View.VISIBLE
    }
    // شيمر
    private fun showAreaLoading(loading: Boolean) {
        if (_binding == null) return
        if (loading) {
            binding.shimmerArea.visibility = View.VISIBLE
            binding.shimmerArea.startShimmer()
            binding.newItemsRecyclerView.visibility = View.GONE
        } else {
            binding.shimmerArea.stopShimmer()
            binding.shimmerArea.visibility = View.GONE
            binding.newItemsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showAllLoading(loading: Boolean) {
        if (_binding == null) return
        if (loading) {
            binding.shimmerAll.visibility = View.VISIBLE
            binding.shimmerAll.startShimmer()
            binding.cardRecyclerView.visibility = View.GONE
        } else {
            binding.shimmerAll.stopShimmer()
            binding.shimmerAll.visibility = View.GONE
            binding.cardRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateSectionVisibility(showSearch: Boolean) {
        if (_binding == null) return
        binding.homeContent.visibility = if (showSearch) View.GONE else View.VISIBLE
        binding.searchResultsRecyclerView.visibility = if (showSearch) View.VISIBLE else View.GONE
    }



    private fun listenUnreadNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return

        // أوقف أي listener قديم
        notificationsListener?.remove()

        notificationsListener = db.collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("seen", false)
            .addSnapshotListener { snapshot, error ->
                if (_binding == null) return@addSnapshotListener
                if (error != null) return@addSnapshotListener

                val unreadCount = snapshot?.size() ?: 0
                binding.notificationBadge.apply {
                    if (unreadCount > 0) {
                        visibility = View.VISIBLE
                        text = unreadCount.toString()
                    } else {
                        visibility = View.GONE
                    }
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // أوقف كل الـ listeners عشان ما يضلوا شغالين
        areaReg?.remove(); areaReg = null
        allReg?.remove(); allReg = null
        searchReg?.remove(); searchReg = null
        userDocReg?.remove(); userDocReg = null

        // ألغي أي ديباونس شغال
        searchDebounce?.let { binding.searchBox.removeCallbacks(it) }
        searchDebounce = null

        _binding = null
    }
    override fun onResume() {
        super.onResume()
        listenUnreadNotifications()
    }
}