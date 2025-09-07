package com.example.swapit1.Onboarding

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.swapit1.databinding.FragmentOnboardingScreenBinding
import androidx.core.content.edit
import com.example.swapit1.Registration.LoginActivity

class OnboardingImageFragment(private val pageIndex: Int) : Fragment() {

    private var _binding: FragmentOnboardingScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingScreenBinding.inflate(inflater, container, false)

        binding.image1.visibility = View.GONE
        binding.image2.visibility = View.GONE
        binding.image3.visibility = View.GONE

        when (pageIndex) {
            0 -> binding.image1.visibility = View.VISIBLE
            1 -> binding.image2.visibility = View.VISIBLE
            2 -> binding.image3.visibility = View.VISIBLE
        }

        binding.btnStart.visibility = if (pageIndex == 2) View.VISIBLE else View.GONE

        binding.btnStart.setOnClickListener {
            requireContext()
                .getSharedPreferences("app_pref", MODE_PRIVATE)
                .edit {
                    putBoolean("seen_onboarding", true)
                }

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            // أو: requireActivity().finish()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
