/*package com.example.swapit1.Registration

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.databinding.ActivityForgotPasswordBinding
import com.example.swapit1.databinding.DialogVerifyCodeBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var verificationId: String
    private var enteredPhone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.sendCodeButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال رقم الجوال", Toast.LENGTH_SHORT).show()
            } else {
                enteredPhone = phone
                sendVerificationCode("+972$phone") // عدل رمز الدولة إذا لزم
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // تخطي إدخال الكود إذا تحقق تلقائيًا (نادرًا)
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@ForgotPasswordActivity, "فشل إرسال الكود: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            showCodeDialog()
        }
    }

    private fun showCodeDialog() {
        val dialogBinding = DialogVerifyCodeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            val code = dialogBinding.codeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code)
                dialog.dismiss()
            } else {
                dialogBinding.codeInput.error = "يرجى إدخال الكود"
            }
        }

        dialog.show()
    }
    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("phone", enteredPhone)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "فشل التحقق من الكود", Toast.LENGTH_LONG).show()
                }
            }
    }
}*/
