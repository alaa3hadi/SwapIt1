package com.example.swapit1.core

import android.content.Context

object SessionPrefs {
    private const val PREFS_NAME   = "app_prefs"
    private const val KEY_USER_ID  = "user_id"

    fun saveUserId(context: Context, uid: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, uid)
            .apply()
    }

    fun getUserId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER_ID)
            .apply()
    }
}
