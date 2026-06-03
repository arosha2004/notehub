package com.example.notehub.data.remote

import com.example.notehub.domain.model.LocationNote
import com.google.gson.annotations.SerializedName

/**
 * LocationNoteDto — Represents the JSON structure shared with the Laravel API.
 */
data class LocationNoteDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("color_hex") val colorHex: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("is_secured") val isSecured: Int?,
    @SerializedName("security_password") val securityPassword: String?
)

/**
 * Extension to convert data DTO to Domain model.
 */
fun LocationNoteDto.toDomain(): LocationNote {
    return LocationNote(
        id = id ?: 0,
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address ?: "Unknown Address",
        date = createdAt?.take(10) ?: "Just now",
        category = category ?: "Personal",
        colorHex = colorHex ?: "#6366F1",
        isSecured = (isSecured ?: 0) == 1,
        securityPassword = securityPassword
    )
}

/**
 * Extension to convert Domain model back to DTO.
 */
fun LocationNote.toDto(): LocationNoteDto {
    return LocationNoteDto(
        id = id.takeIf { it != 0 },
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address,
        category = category,
        colorHex = colorHex,
        createdAt = date,
        isSecured = if (isSecured) 1 else 0,
        securityPassword = securityPassword
    )
}
