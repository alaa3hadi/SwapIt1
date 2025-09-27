
package com.example.swapit1.Registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.swapit1.MainActivity
import com.example.swapit1.NotificationHelper
import com.example.swapit1.R
import com.example.swapit1.Registration.SignUpActivity
import com.example.swapit1.auth.AuthAlias
import com.example.swapit1.databinding.ActivityLoginBinding
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // مفاتيح SharedPreferences
    private val PREFS_NAME = "app_prefs"
    private val KEY_USER_ID = "user_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // امسح الأخطاء عند الكتابة
        binding.phoneEditText.doOnTextChanged { _, _, _, _ -> binding.tilPhone.error = null }
        binding.passwordEditText.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }

        binding.loginButton.setOnClickListener { attemptLogin() }

        binding.goToSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.forgotPasswordText.setOnClickListener {
            // startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun attemptLogin() {
        // امسح الأخطاء
        binding.tilPhone.error = null
        binding.tilPassword.error = null

        val phoneRaw = binding.phoneEditText.text?.toString()?.trim().orEmpty()
        val password = binding.passwordEditText.text?.toString()?.trim().orEmpty()

        var ok = true
        if (phoneRaw.isEmpty())  { binding.tilPhone.error = "يرجى إدخال رقم الجوال"; ok = false }
        if (password.isEmpty())  { binding.tilPassword.error = "يرجى إدخال كلمة المرور"; ok = false }
        if (!ok) return

        // (1) تحقق محلي صارم: 10 أرقام وتبدأ بـ 059 أو 056 — بدون أي رموز أو +
        val phoneErr = AuthAlias.validateLocalPs(phoneRaw)
        if (phoneErr != null) {
            binding.tilPhone.error = phoneErr
            return
        }

        // (2) حوّل الرقم المحلي إلى E.164 على +97059xxxxxxx
        val e164Main = AuthAlias.localToE164Ps(phoneRaw)

        // بديل للتوافق مع حسابات قديمة (+972)
        val e164Alt = if (e164Main.startsWith("+970"))
            e164Main.replaceFirst("+970", "+972")
        else e164Main

        Log.d("LOGIN", "inputLocal=$phoneRaw  e164Main=$e164Main  e164Alt=$e164Alt")

        // (3) ابحث عن المستخدم في Firestore users حسب phone
        findUserByPhone(
            e164Main,
            onFound = { phoneFromDoc ->
                val aliasEmail = AuthAlias.phoneToAliasEmail(phoneFromDoc)
                signInWithAlias(aliasEmail, password)
            },
            onNotFound = {
                if (e164Alt != e164Main) {
                    findUserByPhone(
                        e164Alt,
                        onFound = { phoneFromDoc ->
                            val aliasEmail = AuthAlias.phoneToAliasEmail(phoneFromDoc)
                            signInWithAlias(aliasEmail, password)
                        },
                        onNotFound = {
                            binding.tilPhone.error = "لا يوجد حساب بهذا الرقم"
                            binding.tilPassword.error = null
                        }
                    )
                } else {
                    binding.tilPhone.error = "لا يوجد حساب بهذا الرقم"
                    binding.tilPassword.error = null
                }
            }
        )
    }

    /**
     * يبحث في مجموعة users عن مستند phone == e164 (limit=1)
     * عند النجاح يُعيد نفس قيمة الهاتف الموجودة في المستند (لنستنتج منها alias الصحيح)
     */
    private fun findUserByPhone(
        e164: String,
        onFound: (phoneFromDoc: String) -> Unit,
        onNotFound: () -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("phone", e164)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                Log.d("LOGIN", "firestore users? phone=$e164 found=${doc != null}")
                if (doc != null) {
                    val phoneFromDoc = doc.getString("phone") ?: e164
                    onFound(phoneFromDoc)
                } else {
                    onNotFound()
                }
            }
            .addOnFailureListener { err ->
                Log.e("LOGIN", "firestore query failed: ${err.localizedMessage}", err)
                binding.tilPassword.error = "تعذر التحقق الآن، حاول لاحقًا"
            }
    }

    /**
     * يجرّب تسجيل الدخول بـ Email/Password مبنيّ من الهاتف الموجود فعليًا في Firestore
     * وعند النجاح يحفظ UID في SharedPreferences ثم يذهب للصفحة الرئيسية.
     */
    private fun signInWithAlias(aliasEmail: String, password: String) {
        Log.d("LOGIN", "signIn alias=$aliasEmail")
        auth.signInWithEmailAndPassword(aliasEmail, password)
            .addOnSuccessListener { cred ->
                val uid = cred.user?.uid ?: auth.currentUser?.uid
                if (!uid.isNullOrEmpty()) {
                    saveUserId(uid)
                }
                // ➤ إشعار على واجهة التطبيق وFirestore مع type = "login"
                addLoginNotification(uid ?: return@addOnSuccessListener)

                // ➤ إشعار نظامي على الجوال
                NotificationHelper.showNotification(
                    this,
                    "تم تسجيل الدخول بنجاح، مرحبًا بك من جديد!",
                    "",
                    System.currentTimeMillis().toInt(),
                    R.drawable.swapit
                )

                goHome()
            }
            .addOnFailureListener { ex ->
                val code = (ex as? FirebaseAuthException)?.errorCode ?: "NO_CODE"
                Log.e("LOGIN", "signIn failed: alias=$aliasEmail type=${ex::class.java.simpleName} code=$code msg=${ex.localizedMessage}", ex)

                when {
                    // كلمة المرور/الاعتماد غير صحيحة → نفس الرسالة على الحقلين
                    ex is FirebaseAuthInvalidCredentialsException ||
                            code == "ERROR_WRONG_PASSWORD" ||
                            code == "ERROR_INVALID_CREDENTIAL" -> {
                        val msg = "رقم الجوال أو كلمة المرور غير صحيحة"
                        binding.tilPhone.error = msg
                        binding.tilPassword.error = msg
                    }

                    // لو Firebase رجّع فجأة not found (يفترض ما يصير بعد ما لقينا المستند)
                    ex is FirebaseAuthInvalidUserException || code == "ERROR_USER_NOT_FOUND" -> {
                        binding.tilPhone.error = "لا يوجد حساب بهذا الرقم"
                        binding.tilPassword.error = null
                    }

                    code == "ERROR_INVALID_EMAIL" -> {
                        binding.tilPhone.error = "رقم الجوال غير صالح"
                        binding.tilPassword.error = null
                    }

                    else -> {
                        binding.tilPassword.error = "تعذر تسجيل الدخول، حاول لاحقًا"
                        toast(ex.localizedMessage ?: "تعذر تسجيل الدخول، حاول لاحقًا")
                    }
                }
            }
    }

    private fun saveUserId(uid: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, uid)
            .apply()
    }


    // داله الاشعارات
    private fun addLoginNotification(userId: String) {
        val notifData = hashMapOf(
            "userId" to userId,
            "title" to "تم تسجيل الدخول بنجاح، مرحبًا بك من جديد!",
            "message" to "",
            "type" to "login",           // ➤ النوع لتحديد أيقونة واجهة التطبيق
            "createdAt" to com.google.firebase.Timestamp.now(),
            "seen" to false
        )
        db.collection("notifications").add(notifData)
            .addOnSuccessListener { Log.d("LOGIN", "Login notification added: ${it.id}") }
            .addOnFailureListener { e -> Log.e("LOGIN", "Failed to add login notification", e) }
    }

    private fun goHome() {
        // لو لسببٍ ما ما انحفظ uid فوق، جرّب تحفظه هنا أيضًا
        val uid = auth.currentUser?.uid
        if (!uid.isNullOrEmpty()) {
            saveUserId(uid)
        }

        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // (اختياري) تمريره كإكسترا أيضًا:
            putExtra("uid", uid)
        })
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}