package com.example.swapit1.ui.details

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityMyRequestDetailsBinding
import com.example.swapit1.model.requestItem

class My_Request_Details : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         val binding = ActivityMyRequestDetailsBinding.inflate(layoutInflater)
          binding.backButton.setOnClickListener {
                 finish()
             }

         setContentView(binding.root)



    }
}