package com.example.swapit1.ui.myAccount

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.R
import com.example.swapit1.Registration.LoginActivity


import com.example.swapit1.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth

class delete_account : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // زر العودة
        binding.backButton.setOnClickListener { finish() }

        // الزر دائمًا مفعل
        binding.deleteAccountButton.isEnabled = true

        // حدث الضغط على زر الحذف
        binding.deleteAccountButton.setOnClickListener {
            if (!binding.confirmCheckBox.isChecked) {
                Toast.makeText(
                    this,
                    "يجب تأكيد فهمك لحذف الحساب",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val reason = binding.deleteReasonInput.text.toString().trim()
            deleteAccount(reason)
        }
    }

    private fun deleteAccount(reason: String) {
        val user = auth.currentUser
        if (user != null) {
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "تم حذف الحساب بنجاح",
                        Toast.LENGTH_SHORT
                    ).show()

                    // بعد الحذف تسجيل الخروج والعودة لشاشة تسجيل الدخول
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "حدث خطأ أثناء حذف الحساب: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "لم يتم العثور على مستخدم مسجل", Toast.LENGTH_SHORT).show()
        }
    }
}
