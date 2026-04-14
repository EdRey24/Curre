package edu.bu.cs411.group10.curre.data

import edu.bu.cs411.group10.curre.network.RetrofitClient
import edu.bu.cs411.group10.curre.ui.model.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactRepository {
    private const val USER_ID = 1L

    suspend fun loadContacts(): Result<List<EmergencyContact>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.contactApi.getContacts(USER_ID)
            }
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    } // END OF FUNCTION loadContacts

    suspend fun addContact(name: String, email: String): Result<EmergencyContact> {
        return try {
            val contact = EmergencyContact(id = 0, name = name, email = email)
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.contactApi.addContact(USER_ID, contact)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Add contact failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    } // END OF FUNCTION addContact

    suspend fun updateContact(contactId: Long, name: String, email: String): Result<EmergencyContact> {
        return try {
            val contact = EmergencyContact(id = contactId, name = name, email = email)
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.contactApi.updateContact(USER_ID, contactId, contact)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Update contact failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    } // END OF FUNCTION updateContact

    suspend fun deleteContact(contactId: Long): Result<Unit> {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.contactApi.deleteContact(USER_ID, contactId)
            }
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete contact failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    } // END OF FUNCTION deleteContact
} // END OF OBJECT ContactRepository