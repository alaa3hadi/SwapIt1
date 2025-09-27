package com.example.swapit1.ui.myAccount

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.swapit1.R
import com.example.swapit1.Registration.LoginActivity
import com.example.swapit1.databinding.FragmentAccountBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class accountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        listenUserData()  // 🔹 تحميل البيانات بشكل مباشر وتلقائي

        binding.profileEditLayout.setOnClickListener {
            val intent = Intent(requireContext(), edit_profile::class.java)
            startActivity(intent)
        }

        binding.changePasswordLayout.setOnClickListener {
            startActivity(Intent(requireContext(), change_password::class.java))
        }
        binding.notificationsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), edit_notifications::class.java))
        }
        binding.contactUsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), contact_us::class.java))
        }
        binding.privacyPolicyLayout.setOnClickListener {
            startActivity(Intent(requireContext(), privacy_policy::class.java))
        }
        binding.deleteAccountLayout.setOnClickListener {
            startActivity(Intent(requireContext(), delete_account::class.java))
        }

        binding.logoutLayout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }

        return root
    }

    // 🔹 تحديث مباشر باستخدام Snapshot Listener
    private fun listenUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "فشل تحميل البيانات: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (doc != null && doc.exists()) {
                    binding.profileName.text = doc.getString("name") ?: "اسم غير معروف"
                    binding.profilePhone.text = doc.getString("phone") ?: ""

                    val photoBase64 = doc.getString("photoBase64")
                    if (!photoBase64.isNullOrEmpty()) {
                        val bytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.ownerImage.setImageBitmap(bitmap)
                    } else {
                        binding.ownerImage.setImageResource(R.drawable.user_icon)
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
