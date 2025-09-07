package com.example.swapit1.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.swapit1.R
import com.example.swapit1.adapter.OrdersTabAdapter
import com.example.swapit1.databinding.FragmentDashboardBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//       (activity as AppCompatActivity).supportActionBar?.hide()
        return inflater.inflate(R.layout.fragment_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        //val tabStrip = tabLayout.getChildAt(0) as ViewGroup


        val adapter = OrdersTabAdapter(this)
        viewPager.adapter = adapter

        val titles = listOf("طلباتي", "عروضي")

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
        tabLayout.post {
            val tabStrip = tabLayout.getChildAt(0) as ViewGroup
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabStrip.getChildAt(i)
                val params = tab.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(3.toDp(requireContext()), 3.toDp(requireContext()), 3.toDp(requireContext()), 3.toDp(requireContext()))
                tab.layoutParams = params
                tab.invalidate()
            }
        }




    }
    fun Int.toDp(context : Context): Int = (this * context.resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        // إظهار الـ ActionBar عند الخروج من الفراقمينت
//        (activity as AppCompatActivity).supportActionBar?.show()
    }
}

