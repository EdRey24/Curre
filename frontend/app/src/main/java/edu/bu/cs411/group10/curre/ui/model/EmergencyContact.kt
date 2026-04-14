package edu.bu.cs411.group10.curre.ui.model

data class EmergencyContact(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String? = null
)


data class EmergencyContactDto(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String? = null
)