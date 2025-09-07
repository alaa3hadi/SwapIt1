package com.example.swapit1.ui.myAccount

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.swapit1.R
import com.example.swapit1.databinding.ActivityContactUsBinding

class contact_us : AppCompatActivity() {
    private lateinit var binding: ActivityContactUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


            // تفعيل الـ ViewBinding
            binding = ActivityContactUsBinding.inflate(layoutInflater)
            setContentView(binding.root)


            binding.toolbar.setNavigationOnClickListener {
                // عند الضغط على الرجوع، العودة إلى شاشة البروفايل
                finish()
            }

            // عند الضغط على البريد الإلكتروني
            binding.emailTextView.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:swapIt87@gmail.com")
                }
                startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }
            binding.phoneTextView.setOnClickListener {
                val phoneNumber = "‪+972595040979‬"
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            }

        binding.backButton.setOnClickListener {
            finish()
        }
        }
    }
