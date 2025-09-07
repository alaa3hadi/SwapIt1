package com.example.swapit1.ui.myAccount

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.databinding.ActivityPrivacyPolicyBinding

class privacy_policy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        binding.backButton.setOnClickListener {
            finish()
        }

        setContentView(binding.root)



    }
}