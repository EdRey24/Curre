package edu.bu.cs411.group10.curre.network

import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import retrofit2.Response
import retrofit2.http.*

interface ContactApiService {
    @GET("/api/contacts")
    suspend fun getContacts(@Header("X-User-Id") userId: Long): Response<List<EmergencyContact>>

    @POST("/api/contacts")
    suspend fun addContact(@Header("X-User-Id") userId: Long, @Body contact: EmergencyContact): Response<EmergencyContact>

    @PUT("/api/contacts/{contactId}")
    suspend fun updateContact(@Header("X-User-Id") userId: Long, @Path("contactId") contactId: Long, @Body contact: EmergencyContact): Response<EmergencyContact>

    @DELETE("/api/contacts/{contactId}")
    suspend fun deleteContact(@Header("X-User-Id") userId: Long, @Path("contactId") contactId: Long): Response<Unit>
}