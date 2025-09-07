package com.example.swapit1.ui.myAccount

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.swapit1.R
import com.example.swapit1.databinding.FragmentAccountBinding
import com.example.swapit1.databinding.FragmentHomeBinding
import com.example.swapit1.ui.home.HomeViewModel
import org.antlr.v4.runtime.misc.MurmurHash.finish


class accountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root



        // تعديل الملف الشخصي
        binding.profileEditLayout.setOnClickListener {

            startActivity(Intent(requireContext(), edit_profile::class.java))
        }

        // تغيير كلمة المرور
        binding.changePasswordLayout.setOnClickListener {
            startActivity(Intent(requireContext(), change_password::class.java))
        }

        // الإشعارات
        binding.notificationsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), edit_notifications::class.java))
        }

        // تواصل معنا
        binding.contactUsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), contact_us::class.java))
        }
        binding.privacyPolicyLayout.setOnClickListener {
            startActivity(Intent(requireContext(), privacy_policy::class.java))
        }
        binding.deleteAccountLayout.setOnClickListener {
            startActivity(Intent(requireContext(), delete_account::class.java))
        }

        // تسجيل خروج
        binding.logoutLayout.setOnClickListener {
            Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()

            // val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
           }


    return root
    }
}