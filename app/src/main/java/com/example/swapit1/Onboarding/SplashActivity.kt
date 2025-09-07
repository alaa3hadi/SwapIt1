package com.example.swapit1.Onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.Registration.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_pref", MODE_PRIVATE)
        val seenOnboarding = sharedPref.getBoolean("seen_onboarding", false)

        if (seenOnboarding) {
            startActivity(Intent(this, LoginActivity::class.java))

        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        finish()
    }
}
