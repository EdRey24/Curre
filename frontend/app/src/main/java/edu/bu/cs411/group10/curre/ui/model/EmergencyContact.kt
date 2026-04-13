package edu.bu.cs411.group10.curre.ui.model

data class EmergencyContact(
    val id: Long,          // change from Int to Long to match backend
    val name: String,
    val email: String,
    val phone: String? = null
)