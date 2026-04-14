package edu.bu.cs411.group10.curre.network

import edu.bu.cs411.group10.curre.ui.model.EmergencyContactDto
import retrofit2.Response
import retrofit2.http.*

interface ContactApiService {
    @GET("/api/contacts")
    suspend fun getContacts(): Response<List<EmergencyContactDto>>

    @POST("/api/contacts")
    suspend fun addContact(@Body contact: EmergencyContactDto): Response<EmergencyContactDto>

    @PUT("/api/contacts/{id}")
    suspend fun updateContact(
        @Path("id") id: Long,
        @Body contact: EmergencyContactDto
    ): Response<EmergencyContactDto>

    @DELETE("/api/contacts/{id}")
    suspend fun deleteContact(@Path("id") id: Long): Response<Unit>
}