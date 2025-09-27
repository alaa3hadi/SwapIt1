package com.example.swapit1.ui.myAccount

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth

class change_password : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        binding.changePasswordButton.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val current = binding.currentPasswordEditText.text.toString().trim()
        val newPass = binding.newPasswordEditText.text.toString().trim()
        val confirm = binding.confirmPasswordEditText.text.toString().trim()
        var isValid = true

        if (current.isEmpty()) {
            binding.currentPasswordEditText.error = "يرجى إدخال كلمة المرور الحالية"
            isValid = false
        } else binding.currentPasswordEditText.error = null

        if (newPass.isEmpty()) {
            binding.newPasswordEditText.error = "يرجى إدخال كلمة المرور الجديدة"
            isValid = false
        } else binding.newPasswordEditText.error = null

        if (confirm.isEmpty()) {
            binding.confirmPasswordEditText.error = "يرجى تأكيد كلمة المرور"
            isValid = false
        } else if (confirm != newPass) {
            binding.confirmPasswordEditText.error = "كلمة المرور غير متطابقة"
            isValid = false
        } else binding.confirmPasswordEditText.error = null

        if (!isValid) return

        val user = auth.currentUser
        if (user?.email != null) {
            auth.signInWithEmailAndPassword(user.email!!, current)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPass).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "تم تغيير كلمة المرور بنجاح", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "فشل تغيير كلمة المرور: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.currentPasswordEditText.error = "كلمة المرور الحالية غير صحيحة"
                    }
                }
        }
    }
}
