package com.example.swapit1.edit
import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.swapit1.databinding.ActivityEditRequestBinding
import com.example.swapit1.ui.OffersRequests.MyRequestsFragment

class edit_request : AppCompatActivity() {
    private lateinit var binding : ActivityEditRequestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val categories = listOf("طعام", "ملابس", "اطفال", "الكترونيات","اثاث ","اخرى")
        val categoryAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        val locations = listOf("غزة", " شمال غزة", "وسطى", "جنوب")
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        binding.locationSpinner.setAdapter(locationAdapter)
        binding.categorySpinner.setOnClickListener {
            binding.categorySpinner.showDropDown()
        }

        binding.locationSpinner.setOnClickListener {
            binding.locationSpinner.showDropDown()
        }

        binding.backButton.setOnClickListener {

          finish()

        }

    }
}