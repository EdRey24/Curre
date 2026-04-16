package edu.bu.cs411.group10.curre.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(
    val email: String,
    val password: String,
    val confirmPassword: String? = null
)

data class AuthResponse(
    val userId: Long?,
    val email: String?,
    val message: String
)

interface AuthApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}
