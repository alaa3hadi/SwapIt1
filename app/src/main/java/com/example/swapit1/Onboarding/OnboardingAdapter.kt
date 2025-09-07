package com.example.swapit1.Onboarding

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3 // ثلاث صفحات

    override fun createFragment(position: Int): Fragment {
        return OnboardingImageFragment(position)
    }
}
