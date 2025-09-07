package com.example.swapit1.ui.myAccount

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityDeleteAccountBinding
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding

class delete_account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        binding.backButton.setOnClickListener {
            finish()
        }

        setContentView(binding.root)



    }
}