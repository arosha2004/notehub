package com.example.notehub.data.repository

import android.location.Location
import android.util.Log
import com.example.notehub.data.remote.RetrofitClient
import com.example.notehub.data.remote.toDomain
import com.example.notehub.data.remote.toDto
import com.example.notehub.domain.model.LocationNote
import com.example.notehub.domain.repository.LocationNotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LocationNotesRepositoryImpl — Implements the LocationNotesRepository interface.
 * Uses a Dual-Mode design:
 * 1. Synchronizes with the Laravel JWT backend via Retrofit.
 * 2. If the backend is offline or throws a network error, it transparently falls back
 *    to a local in-memory database preloaded with sample data to ensure the app is fully functional.
 */
class LocationNotesRepositoryImpl : LocationNotesRepository {

    private val tag = "LocationNotesRepo"

    // Local in-memory store for fallback operations
    private val localNotes = mutableListOf<LocationNote>()

    init {
        // Pre-populate with beautiful, realistic mock notes across different locations
        // Colombo, Sri Lanka coordinates (approx. 6.9271, 79.8612)
        localNotes.add(
            LocationNote(
                id = 101,
                title = "Galle Face Green Sunset Spot",
                description = "Great location to catch the sunset. Ideal place to review design proposals or read book chapters.",
                latitude = 6.9270,
                longitude = 79.8400,
                address = "Galle Face Green, Colombo 03, Sri Lanka",
                date = "2026-05-18",
                category = "Personal",
                colorHex = "#6366F1" // Indigo
            )
        )
        localNotes.add(
            LocationNote(
                id = 102,
                title = "Colombo Office - Standup Meeting",
                description = "Standup at 9 AM daily. Bring project documents and sprint boards.",
                latitude = 6.9285,
                longitude = 79.8650,
                address = "Trace Expert City, Maradana Rd, Colombo 01000",
                date = "2026-05-19",
                category = "Work",
                colorHex = "#10B981" // Success Green
            )
        )

        // San Francisco coordinates (approx 37.7749, -122.4194) for default emulator locations
        localNotes.add(
            LocationNote(
                id = 103,
                title = "Golden Gate Bridge Overlook",
                description = "Scenic viewpoint. Remember to snap photo for the NoteHub uploads folder and review marketing guidelines.",
                latitude = 37.8199,
                longitude = -122.4783,
                address = "Golden Gate Bridge, San Francisco, CA 94129",
                date = "2026-05-15",
                category = "Study",
                colorHex = "#8B5CF6" // Purple
            )
        )
        localNotes.add(
            LocationNote(
                id = 104,
                title = "Silicon Valley Tech Cafe",
                description = "Strong Wi-Fi, nice working tables. Finish kotlin geofence integration models here.",
                latitude = 37.7749,
                longitude = -122.4194,
                address = "Market St, San Francisco, CA 94103",
                date = "2026-05-20",
                category = "Work",
                colorHex = "#3B82F6" // Info Blue
            )
        )
    }

    override suspend fun getNotes(): Result<List<LocationNote>> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Attempting to fetch notes from PHP backend...")
            val apiResponse = RetrofitClient.api.getNotes()
            if (apiResponse.success && apiResponse.data != null) {
                val domainNotes = apiResponse.data.notes.map { it.toDomain() }
                Log.d(tag, "Successfully loaded ${domainNotes.size} notes from API.")
                Result.success(domainNotes)
            } else {
                Log.w(tag, "API returned failure: ${apiResponse.message}")
                Result.success(ArrayList(localNotes))
            }
        } catch (e: Exception) {
            Log.w(tag, "PHP Backend unreachable. Falling back to local mock storage. Error: ${e.localizedMessage}")
            Result.success(ArrayList(localNotes))
        }
    }

    override suspend fun createNote(
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String
    ): Result<LocationNote> = withContext(Dispatchers.IO) {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newId = if (localNotes.isEmpty()) 1 else (localNotes.maxOf { it.id } + 1)
        
        val newNote = LocationNote(
            id = newId,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            address = address,
            date = dateString,
            category = category,
            colorHex = colorHex
        )

        try {
            Log.d(tag, "Attempting to sync new note to PHP backend...")
            val apiResponse = RetrofitClient.api.createNote(newNote.toDto())
            if (apiResponse.success && apiResponse.data != null) {
                val savedNote = apiResponse.data.note.toDomain()
                
                // Sync with local memory cache just in case
                localNotes.add(0, savedNote)
                Log.d(tag, "Successfully synced note to API with ID: ${savedNote.id}")
                Result.success(savedNote)
            } else {
                Log.w(tag, "API error on create: ${apiResponse.message}")
                localNotes.add(0, newNote)
                Result.success(newNote)
            }
        } catch (e: Exception) {
            Log.w(tag, "PHP backend offline. Saving note locally. Error: ${e.localizedMessage}")
            localNotes.add(0, newNote)
            Result.success(newNote)
        }
    }

    override suspend fun deleteNote(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Attempting to delete note $id from PHP backend...")
            val response = RetrofitClient.api.deleteNote(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(tag, "Successfully deleted note $id from API.")
            } else {
                Log.w(tag, "API responded with error: ${response.body()?.message ?: response.code()}")
            }
        } catch (e: Exception) {
            Log.w(tag, "PHP backend offline. Deleting locally. Error: ${e.localizedMessage}")
        }
        
        // Remove locally anyway
        localNotes.removeAll { it.id == id }
        Result.success(Unit)
    }

    override suspend fun getNearbyNotes(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Result<List<LocationNote>> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Attempting to fetch nearby notes from PHP backend...")
            // We use the regular list endpoint and filter locally by proximity to maintain accuracy
            val apiResponse = RetrofitClient.api.getNotes()
            if (apiResponse.success && apiResponse.data != null) {
                val allNotes = apiResponse.data.notes.map { it.toDomain() }
                val filtered = allNotes.filter { note ->
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        latitude, longitude,
                        note.latitude, note.longitude,
                        results
                    )
                    val distanceInKm = results[0] / 1000.0
                    distanceInKm <= radiusInKm
                }
                Result.success(filtered)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.w(tag, "PHP backend offline. Filtering local notes by radius. Error: ${e.localizedMessage}")
            
            val filtered = localNotes.filter { note ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    latitude, longitude,
                    note.latitude, note.longitude,
                    results
                )
                val distanceInKm = results[0] / 1000.0
                distanceInKm <= radiusInKm
            }
            Result.success(filtered)
        }
    }
}
