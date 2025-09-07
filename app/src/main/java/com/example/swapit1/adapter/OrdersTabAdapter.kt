package com.example.swapit1.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.swapit1.ui.OffersRequests.MyOffersFragment
import com.example.swapit1.ui.OffersRequests.MyRequestsFragment

class OrdersTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyRequestsFragment()
            1 -> MyOffersFragment()
            else -> MyRequestsFragment()
        }
    }
}
