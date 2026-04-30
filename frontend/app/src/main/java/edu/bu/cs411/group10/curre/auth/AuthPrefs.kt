package edu.bu.cs411.group10.curre.auth

import android.content.Context
import android.content.SharedPreferences

object AuthPrefs {
    private const val PREFS_NAME = "curre_auth"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLogin(context: Context,
                  userId: Long,
                  email: String,
                  firstName: String? = null,
                  lastName: String? = null
    ) {
        getPrefs(context).edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getLong(KEY_USER_ID, -1L) != -1L
    }

    fun getUserId(context: Context): Long {
        return getPrefs(context).getLong(KEY_USER_ID, -1L)
    }

    fun getEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_EMAIL, null)
    }


    fun getFirstName(context: Context): String? {
        return getPrefs(context).getString(KEY_FIRST_NAME, null)
    }

    fun getLastName(context: Context): String? {
        return getPrefs(context).getString(KEY_LAST_NAME, null)
    }
    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
