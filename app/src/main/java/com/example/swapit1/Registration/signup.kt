package com.example.swapit1.Registration

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.swapit1.auth.AuthAlias
import com.example.swapit1.databinding.ActivitySignupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth.useAppLanguage()

        // قائمة المناطق
        val locations = listOf("شمال غزة", "غزة", "الوسطى", "جنوب")
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
        showLoadingDialog()  // استدعاء واحد فقط

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
        if (name.isEmpty())      {
            binding.tilFullName.error = "يرجى إدخال الاسم الكامل"
            ok = false
        }
        if (phoneRaw.isEmpty())  {
            binding.tilPhone.error    = "يرجى إدخال رقم الجوال"
            ok = false
        }
        if (password.isEmpty())  {
            binding.tilPassword.error = "يرجى إدخال كلمة المرور"
            ok = false
        }
        if (confirm.isEmpty())   {
            binding.tilConfirm.error  = "يرجى تأكيد كلمة المرور"
            ok = false
        }
        if (location.isEmpty())  {
            binding.tilLocation.error = "يرجى اختيار المنطقة"
            ok = false
        }
        if (!ok) {
            loadingDialog?.dismiss()
            return
        }

        if (password.length < 8) {
            loadingDialog?.dismiss()
            binding.tilPassword.error = "كلمة المرور يجب أن تكون 8 أحرف على الأقل"
            return
        }
        if (password != confirm) {
            loadingDialog?.dismiss()
            binding.tilConfirm.error = "كلمات المرور غير متطابقة"
            return
        }

        // تحقق رقم الهاتف
        val phoneErr = AuthAlias.validateLocalPs(phoneRaw)
        if (phoneErr != null) {
            loadingDialog?.dismiss()
            binding.tilPhone.error = phoneErr
            return
        }

        val e164 = AuthAlias.localToE164Ps(phoneRaw)
        val aliasEmail = AuthAlias.phoneToAliasEmail(e164)

        auth.fetchSignInMethodsForEmail(aliasEmail)
            .addOnSuccessListener { res ->
                if (res.signInMethods?.isNotEmpty() == true) {
                    loadingDialog?.dismiss()
                    binding.tilPhone.error = "رقم الجوال مستخدم بالفعل"
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(aliasEmail, password)
                    .addOnSuccessListener { cred ->
                        loadingDialog?.dismiss()

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
                                // تأكد أن الـ dialog مغلق (يمكن استدعاء dismiss مرة أخرى بأمان)
                                loadingDialog?.dismiss()

                                // بعد إنشاء الحساب بنجاح، انتقل إلى MainActivity (التي تعرض HomeFragment)
                                val intent = Intent(this, com.example.swapit1.MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                loadingDialog?.dismiss()
                                toast("تعذر حفظ بيانات المستخدم، حاول لاحقًا")
                            }

                    }
                    .addOnFailureListener { e ->
                        loadingDialog?.dismiss()

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
                loadingDialog?.dismiss()
                toast("تعذر التحقق حالياً، حاول لاحقاً")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun showLoadingDialog() {
        val progressBar = ProgressBar(this).apply { isIndeterminate = true }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50,50,50,50)
            addView(progressBar)
            addView(TextView(context).apply {
                text = "جاري انشاء الحساب..."
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0,20,0,0)
                gravity = Gravity.CENTER
            })
        }
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(container)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }
}
