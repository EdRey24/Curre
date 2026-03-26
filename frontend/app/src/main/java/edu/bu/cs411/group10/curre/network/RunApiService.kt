package edu.bu.cs411.group10.curre.network

import edu.bu.cs411.group10.curre.ui.model.RunDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RunApiService {
    @POST("/api/runs")
    suspend fun saveRun(@Body run: RunDto): Response<RunDto>

    @GET("/api/runs")
    suspend fun getRuns(): Response<List<RunDto>>
}