package edu.bu.cs411.group10.curre.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserIdInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: RunApiService by lazy {
        retrofit.create(RunApiService::class.java)
    }

    val contactApi: ContactApiService by lazy {
        retrofit.create(ContactApiService::class.java)
    }

    val safetyApi: SafetyApiService by lazy {
        retrofit.create(SafetyApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}