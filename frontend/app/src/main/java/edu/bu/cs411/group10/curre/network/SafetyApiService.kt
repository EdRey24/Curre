package edu.bu.cs411.group10.curre.network

import retrofit2.Response
import retrofit2.http.*

data class StartSafetyRequest(
    val runId: Long,
    val intervalSeconds: Int = 900   // 15 minutes default
)

interface SafetyApiService {
    @POST("/api/safety/start")
    suspend fun startSafety(@Body request: StartSafetyRequest): Response<Unit>

    @POST("/api/safety/checkin/{runId}")
    suspend fun checkIn(@Path("runId") runId: Long): Response<Unit>

    @POST("/api/safety/stop/{runId}")
    suspend fun stopSafety(@Path("runId") runId: Long): Response<Unit>
}