package edu.bu.cs411.group10.curre.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class UserIdInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("curre_auth", Context.MODE_PRIVATE)
        val userId = prefs.getLong("user_id", -1L)

        val original = chain.request()
        val requestBuilder = original.newBuilder()

        if (userId != -1L) {
            requestBuilder.header("X-User-Id", userId.toString())
        }

        return chain.proceed(requestBuilder.method(original.method, original.body).build())
    }
}