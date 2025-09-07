/*package com.example.swapit1.Registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapit1.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // نحن هنا بعد التحقق بالهاتف (المستخدم داخل بجلسة حديثة)
        val e164  = intent.getStringExtra("e164") ?: ""
        val alias = intent.getStringExtra("alias") ?: ""

        binding.resetPasswordButton.setOnClickListener {
            val newPass = binding.newPasswordEditText.text.toString().trim()
            val confirm = binding.confirmPasswordEditText.text.toString().trim()

            if (!validateNewPasswords(newPass, confirm)) return@setOnClickListener

            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "يرجى إعادة التحقق برقم الهاتف أولاً", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // هل الحساب مرتبط أصلًا بـ Email/Password؟
            if (alias.isBlank()) {
                // ما وصل alias لأي سبب: حدّث مباشرة
                updatePasswordDirect(newPass)
                return@setOnClickListener
            }

            auth.fetchSignInMethodsForEmail(alias)
                .addOnSuccessListener { res ->
                    val methods = res.signInMethods ?: emptyList()
                    if (methods.contains("password")) {
                        // مزوّد كلمة المرور موجود → حدّث كلمة المرور
                        updatePasswordDirect(newPass)
                    } else {
                        // أول مرة نضيف باسورد: نربط مزوّد Email/Password بهذا alias
                        val cred = EmailAuthProvider.getCredential(alias, newPass)
                        user.linkWithCredential(cred)
                            .addOnSuccessListener {
                                Toast.makeText(this, "تم تعيين كلمة المرور", Toast.LENGTH_SHORT).show()
                                goLogin()
                            }
                            .addOnFailureListener { e ->
                                val code = (e as? FirebaseAuthException)?.errorCode ?: ""
                                val msg = when (code) {
                                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "هذا الحساب مرتبط مسبقًا بمزوّد آخر"
                                    "ERROR_PROVIDER_ALREADY_LINKED"   -> "مزوّد كلمة المرور مرتبط سابقًا"
                                    else -> e.localizedMessage ?: "فشل ربط مزوّد كلمة المرور"
                                }
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "تعذّر التحقق من طريقة الدخول، حاول لاحقًا", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updatePasswordDirect(newPass: String) {
        val user = auth.currentUser ?: return
        user.updatePassword(newPass)
            .addOnSuccessListener {
                Toast.makeText(this, "تم تحديث كلمة المرور", Toast.LENGTH_SHORT).show()
                goLogin()
            }
            .addOnFailureListener { e ->
                val code = (e as? FirebaseAuthException)?.errorCode ?: ""
                val msg = when (code) {
                    "ERROR_REQUIRES_RECENT_LOGIN" ->
                        "لأسباب أمنيّة، سجّلي الدخول من جديد ثم حاولي."
                    "WEAK_PASSWORD", "ERROR_WEAK_PASSWORD" ->
                        "كلمة المرور ضعيفة"
                    else -> e.localizedMessage ?: "تعذر تحديث كلمة المرور"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
    }

    private fun validateNewPasswords(newPass: String, confirm: String): Boolean {
        if (newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "يرجى تعبئة كل الحقول", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPass.length < 8) {
            Toast.makeText(this, "كلمة المرور يجب أن تكون 8 أحرف على الأقل", Toast.LENGTH_SHORT).show()
            return false
        }
        if (newPass != confirm) {
            Toast.makeText(this, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }
}*/
