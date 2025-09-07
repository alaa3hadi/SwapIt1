package com.example.swapit1.auth

object AuthAlias {
    private const val DOMAIN = "swapit.local"

    /** يرجع رسالة خطأ إن وُجدت، أو null لو الرقم صالح */
    fun validateLocalPs(raw: String): String? {
        val t = raw.replace("\\s".toRegex(), "")

        return when {
            t.startsWith("+") -> "أدخل الرقم المحلي بدون + (يبدأ بـ 059 أو 056)"
            t.any { !it.isDigit() } -> "الرقم يجب أن يحتوي على أرقام فقط"
            t.length != 10 -> "الرقم يجب أن يتكون من 10 أرقام"
            !(t.startsWith("059") || t.startsWith("056")) -> "الرقم يجب أن يبدأ بـ 059 أو 056"
            else -> null
        }
    }

    /** يحوّل رقمًا محليًا صالحًا (10 أرقام يبدأ بـ 059/056) إلى E.164 على +970 */
    fun localToE164Ps(local10: String): String {
        val digits = local10.filter { it.isDigit() }      // احتياط
        // مثال: 059xxxxxxxx -> +97059xxxxxxxx
        return "+970" + digits.drop(1)
    }

    fun phoneToAliasEmail(e164: String): String =
        e164.replace("+", "") + "@$DOMAIN"

    // (اختياري) لأغراض لوج فقط
    fun debugConfig() = "DOMAIN=$DOMAIN MODE=LOCAL(059/056)+970"
}
