package com.example.swapit1.ui.myAccount

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityEditProfileBinding

class edit_profile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)






        val locationDropdown = findViewById<AutoCompleteTextView>(R.id.locationDropdown)
        val locations = listOf("غزة", "شمال غزة", "وسطى", "جنوب")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        locationDropdown.setAdapter(adapter)

        locationDropdown.setOnClickListener {
            locationDropdown.showDropDown()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}