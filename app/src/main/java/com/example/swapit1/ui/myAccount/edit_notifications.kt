package com.example.swapit1.ui.myAccount

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityEditNotificationsBinding
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding

class edit_notifications : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditNotificationsBinding.inflate(layoutInflater)
        binding.backButton.setOnClickListener {
            finish()
        }

        setContentView(binding.root)



    }
}