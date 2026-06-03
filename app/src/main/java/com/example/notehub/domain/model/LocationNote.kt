package com.example.notehub.domain.model

/**
 * LocationNote — Domain data model representing a note tied to a GPS location.
 */
data class LocationNote(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val date: String,
    val category: String,
    val colorHex: String,
    val isSecured: Boolean = false,
    val securityPassword: String? = null
)
