package edu.bu.cs411.group10.curre.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val PREFS_NAME = "CurrePrefs"
    private const val KEY_BACKEND_URL = "backend_url"
    private const val DEFAULT_URL = "http://localhost:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private lateinit var runApiService: RunApiService
    private lateinit var contactApiService: ContactApiService
    private var isInitialized = false
    private lateinit var prefs: SharedPreferences

    /**
     * Initialises Retrofit using the saved backend URL or the default emulator URL.
     * @param context Application context (used for SharedPreferences).
     * @return true if connection successful, false otherwise.
     */
    suspend fun initialize(context: Context): Boolean {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(KEY_BACKEND_URL, DEFAULT_URL) ?: DEFAULT_URL
        return initializeWithUrl(savedUrl)
    }

    /**
     * Tries to connect to a given base URL. If successful, saves it in SharedPreferences.
     * @param baseUrl The full base URL (including http:// and port, ending with /).
     * @return true if connection successful, false otherwise.
     */
    suspend fun initializeWithUrl(baseUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                // Test the connection
                val testService = retrofit.create(ContactApiService::class.java)
                val response = testService.getContacts(1L)
                if (response.isSuccessful) {
                    runApiService = retrofit.create(RunApiService::class.java)
                    contactApiService = retrofit.create(ContactApiService::class.java)
                    isInitialized = true
                    // Save successful URL
                    prefs.edit().putString(KEY_BACKEND_URL, baseUrl).apply()
                    Log.d(TAG, "Connected to backend at $baseUrl")
                    true
                } else {
                    Log.e(TAG, "Backend at $baseUrl returned error ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to $baseUrl: ${e.message}")
                false
            }
        }
    }

    /**
     * Clears the saved backend URL (useful for resetting).
     */
    fun resetSavedUrl(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_BACKEND_URL).apply()
    }

    val api: RunApiService
        get() = if (::runApiService.isInitialized) runApiService else throw IllegalStateException("RetrofitClient not initialized")

    val contactApi: ContactApiService
        get() = if (::contactApiService.isInitialized) contactApiService else throw IllegalStateException("RetrofitClient not initialized")
}