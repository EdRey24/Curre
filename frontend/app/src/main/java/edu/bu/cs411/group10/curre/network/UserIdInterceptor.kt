package edu.bu.cs411.group10.curre.network

import okhttp3.Interceptor
import okhttp3.Response

class UserIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("X-User-Id", "1")   // Hardcoded demo user ID
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }
}