package com.example.swapit1.ui.notifications

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapit1.R
import com.example.swapit1.adapter.NotificationAdapter
import com.example.swapit1.databinding.FragmentNotificationsBinding
import com.example.swapit1.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.antlr.v4.runtime.misc.MurmurHash.finish

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsList = mutableListOf<Notification>()
    private lateinit var adapter: NotificationAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        adapter = NotificationAdapter(notificationsList)
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(context)
        binding.recyclerNotifications.adapter = adapter
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.clearAllButton.setOnClickListener {

            if (notificationsList.isEmpty()) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("تأكيد الحذف")
                .setMessage("هل أنت متأكد من حذف جميع الإشعارات؟")
                .setPositiveButton("نعم") { _, _ ->

                    // إنشاء Dialog مخصص للتحميل
                    val progressDialog = Dialog(requireContext())
                    progressDialog.setContentView(R.layout.dialog_progress) // هذا XML الذي أرسلته
                    progressDialog.setCancelable(false)
                    progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // مهم لإزالة أي خلفية إضافية
                    progressDialog.show()
                    val currentUserId = auth.currentUser?.uid ?: return@setPositiveButton

                    db.collection("notifications")
                        .whereEqualTo("userId", currentUserId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.isEmpty) {
                                progressDialog.dismiss()
                                return@addOnSuccessListener
                            }

                            val batch = db.batch()
                            snapshot.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }

                            batch.commit().addOnSuccessListener {
                                notificationsList.clear()
                                adapter.notifyDataSetChanged()
                                binding.emptyView.visibility = View.VISIBLE
                                progressDialog.dismiss()
                            }.addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "فشل الحذف، حاول لاحقًا", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "فشل الوصول للإشعارات", Toast.LENGTH_SHORT).show()
                        }

                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
        val currentUserId = auth.currentUser?.uid ?: return binding.root

        db.collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                notificationsList.clear()
                snapshot?.let {
                    if (it.isEmpty) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        it.documents.forEach { doc ->
                            val message = doc.getString("message") ?: ""
                            val title = doc.getString("title") ?: "إشعار"
                            val type = doc.getString("type") ?: "offer"
                            val iconRes = when(type) {
                                "eyes" -> R.drawable.eyes
                                "login" -> R.drawable.key
                                "offer" -> R.drawable.offer
                                "message" -> R.drawable.emailreq
                                "accept3" -> R.drawable.accept3
                                "close" -> R.drawable.close
                                else -> R.drawable.swapit
                            }
                            val timeText = getTimeAgo(doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L)
                            val seen = doc.getBoolean("seen") ?: false

                            notificationsList.add(Notification(iconRes, title, message, timeText, seen))

                            if (!seen) {
                                doc.reference.update("seen", true)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        binding.myToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        return binding.root
    }

    private fun getTimeAgo(timeStamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeStamp
        if (timeStamp == 0L || diff < 60_000L) return "الآن" // أقل من دقيقة = الآن

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 60 -> "منذ $minutes دقيقة"
            hours < 24 -> "منذ $hours ساعة"
            else -> "منذ $days يوم"
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
