package com.example.swapit1.ui.details

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.databinding.ActivityRequestDetailsBinding

class request_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRequestDetailsBinding.inflate(layoutInflater)
        binding.backButton.setOnClickListener {
            finish()
        }

        setContentView(binding.root)



    }
}