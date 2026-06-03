package com.example.notehub.domain.repository

import com.example.notehub.domain.model.LocationNote

/**
 * LocationNotesRepository — Defines the contract for fetching, creating,
 * and deleting location-based notes. Handles clean separation of concerns.
 */
interface LocationNotesRepository {
    
    /**
     * Retrieves all saved location notes.
     */
    suspend fun getNotes(): Result<List<LocationNote>>

    /**
     * Saves a new location note.
     */
    suspend fun createNote(
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String,
        isSecured: Boolean = false,
        securityPassword: String? = null
    ): Result<LocationNote>

    /**
     * Updates an existing note.
     */
    suspend fun updateNote(
        id: Int,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String,
        isSecured: Boolean = false,
        securityPassword: String? = null
    ): Result<LocationNote>

    /**
     * Deletes a location note by its ID.
     */
    suspend fun deleteNote(id: Int): Result<Unit>

    /**
     * Filters location notes that are within a specific radius (in KM) from user.
     */
    suspend fun getNearbyNotes(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Result<List<LocationNote>>
}
