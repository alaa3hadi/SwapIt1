package com.example.swapit1.Registration

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.swapit1.MainActivity
import com.example.swapit1.auth.AuthAlias
import com.example.swapit1.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth.useAppLanguage()

        // قائمة المناطق
        val locations = listOf("شمال غزة", "غزة", "الوسطى", "جنوب غزة")
        binding.locationDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        )
        binding.locationDropdown.setOnClickListener { binding.locationDropdown.showDropDown() }
        binding.locationDropdown.doOnTextChanged { _, _, _, _ -> binding.tilLocation.error = null }

        // امسح الأخطاء عند الكتابة
        binding.fullNameEditText.doOnTextChanged { _, _, _, _ -> binding.tilFullName.error = null }
        binding.phoneEditText.doOnTextChanged    { _, _, _, _ -> binding.tilPhone.error = null }
        binding.passwordEditText.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }
        binding.confirmPasswordEditText.doOnTextChanged { _, _, _, _ ->
            binding.tilConfirm.error = null
        }

        // إنشاء حساب
        binding.signupButton.setOnClickListener { createAccount() }

        // الذهاب لتسجيل الدخول
        binding.goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createAccount() {
        // مسح الأخطاء
        binding.tilFullName.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null
        binding.tilConfirm.error = null
        binding.tilLocation.error = null

        val name     = binding.fullNameEditText.text?.toString()?.trim().orEmpty()
        val phoneRaw = binding.phoneEditText.text?.toString()?.trim().orEmpty()
        val password = binding.passwordEditText.text?.toString()?.trim().orEmpty()
        val confirm  = binding.confirmPasswordEditText.text?.toString()?.trim().orEmpty()
        val location = binding.locationDropdown.text?.toString()?.trim().orEmpty()

        var ok = true
        if (name.isEmpty())      { binding.tilFullName.error = "يرجى إدخال الاسم الكامل"; ok = false }
        if (phoneRaw.isEmpty())  { binding.tilPhone.error    = "يرجى إدخال رقم الجوال"; ok = false }
        if (password.isEmpty())  { binding.tilPassword.error = "يرجى إدخال كلمة المرور"; ok = false }
        if (confirm.isEmpty())   { binding.tilConfirm.error  = "يرجى تأكيد كلمة المرور"; ok = false }
        if (location.isEmpty())  { binding.tilLocation.error = "يرجى اختيار المنطقة"; ok = false }
        if (!ok) return

        if (password.length < 8) {
            binding.tilPassword.error = "كلمة المرور يجب أن تكون 8 أحرف على الأقل"
            return
        }
        if (password != confirm) {
            binding.tilConfirm.error = "كلمات المرور غير متطابقة"
            return
        }

        // تحويل الرقم وبناء alias
        val phoneErr = AuthAlias.validateLocalPs(phoneRaw)
        if (phoneErr != null) {
            binding.tilPhone.error = phoneErr
            return
        }

        val e164 = AuthAlias.localToE164Ps(phoneRaw)
        val aliasEmail = AuthAlias.phoneToAliasEmail(e164)
        // تأكد أن الرقم/الحساب غير مستخدم
        auth.fetchSignInMethodsForEmail(aliasEmail)
            .addOnSuccessListener { res ->
                val methods = res.signInMethods ?: emptyList()
                if (methods.isNotEmpty()) {
                    binding.tilPhone.error = "رقم الجوال مستخدم بالفعل"
                    return@addOnSuccessListener
                }

                // إنشاء الحساب
                auth.createUserWithEmailAndPassword(aliasEmail, password)
                    .addOnSuccessListener { cred ->
                        val uid = cred.user?.uid ?: return@addOnSuccessListener
                        val profile = mapOf(
                            "name"      to name,
                            "phone"     to e164,
                            "location"  to location,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("users").document(uid)
                            .set(profile, SetOptions.merge())
                            .addOnSuccessListener {
                                startActivity(Intent(this, LoginActivity::class.java))
                            }

                    }
                    .addOnFailureListener { e ->
                        val code = (e as? FirebaseAuthException)?.errorCode ?: ""
                        when (code) {
                            "ERROR_WEAK_PASSWORD"        -> binding.tilPassword.error = "كلمة المرور ضعيفة"
                            "ERROR_EMAIL_ALREADY_IN_USE" -> binding.tilPhone.error    = "رقم الجوال مستخدم بالفعل"
                            "ERROR_INVALID_EMAIL"        -> binding.tilPhone.error    = "رقم الجوال غير صالح"
                            else -> toast(e.localizedMessage ?: "تعذر إنشاء الحساب")
                        }
                    }
            }
            .addOnFailureListener {
                toast("تعذر التحقق حالياً، حاول لاحقاً")
            }
    }



    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
