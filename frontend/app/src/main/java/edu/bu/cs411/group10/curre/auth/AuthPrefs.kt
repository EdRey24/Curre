package edu.bu.cs411.group10.curre.auth

import android.content.Context
import android.content.SharedPreferences

object AuthPrefs {
    private const val PREFS_NAME = "curre_auth"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLogin(context: Context, userId: Long, email: String) {
        getPrefs(context).edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getLong(KEY_USER_ID, -1L) != -1L
    }

    fun getUserId(context: Context): Long {
        return getPrefs(context).getLong(KEY_USER_ID, -1L)
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
