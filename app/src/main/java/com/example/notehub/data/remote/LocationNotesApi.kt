package com.example.notehub.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * LocationNotesApi — Retrofit contract mapping endpoints on the Laravel JWT backend.
 */
interface LocationNotesApi {

    @GET("location-notes")
    suspend fun getNotes(): List<LocationNoteDto>

    @POST("location-notes")
    suspend fun createNote(@Body note: LocationNoteDto): LocationNoteDto

    @DELETE("location-notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): Response<Unit>

    @GET("location-notes/nearby")
    suspend fun getNearbyNotes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusInKm: Double
    ): List<LocationNoteDto>
}
