package com.example.swapit1.ui.myAccount

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.databinding.ActivityEditNotificationsBinding

class edit_notifications : AppCompatActivity() {

    private lateinit var binding: ActivityEditNotificationsBinding
    private val PREFS_NAME = "notifications_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // استرجاع الحالة عند فتح الشاشة
        val globalEnabled = prefs.getBoolean("global", true)
        binding.switchGlobal.isChecked = globalEnabled

        // زر الرجوع
        binding.backButton.setOnClickListener {
            finish()
        }

        // السويتش العام
        binding.switchGlobal.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("global", isChecked).apply()
            val msg = if (isChecked) "تم تفعيل جميع الإشعارات" else "تم إيقاف جميع الإشعارات"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

