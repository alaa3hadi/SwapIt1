package com.example.swapit1.Onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.swapit1.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter

        // تابع الزر من آخر Fragment
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // إضافة أي منطق إضافي هنا إن أردت
            }
        })

        // الزر في Fragment الأخير يفتح Login
        // تم وضع onClick مباشرة في XML أو ممكن نربطه هنا إذا أردت
    }
}
