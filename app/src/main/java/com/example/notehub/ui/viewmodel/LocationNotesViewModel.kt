package com.example.notehub.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notehub.data.repository.LocationNotesRepositoryImpl
import com.example.notehub.domain.model.LocationNote
import com.example.notehub.domain.repository.LocationNotesRepository
import com.example.notehub.geofence.GeofenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import androidx.compose.runtime.mutableStateListOf
import com.example.notehub.domain.model.Upload
import com.example.notehub.data.remote.TokenManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AttachFile

/** DTO for storing Upload data locally */
data class UploadDto(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val uploadDate: String,
    val uriString: String?
)

/**
 * LocationNotesViewModel — Orchestrates coordinates capture, reverse-geocoding addresses,
 * saving location notes to the repository, scheduling geofences, and filtering notes.
 */
class LocationNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "LocationNotesVM"
    private val repository: LocationNotesRepository = LocationNotesRepositoryImpl(application)
    private val geofenceManager = GeofenceManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // ── STATE VARIABLES ───────────────────────────────────────────
    
    private val gson = Gson()
    
    // Shared Uploads list
    val uploads = mutableStateListOf<Upload>()
    
    private fun getSanitizedEmail(): String {
        val email = TokenManager.getLoggedInEmail() ?: "guest"
        return email.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
    }

    private fun getUploadsFile(): File {
        return File(getApplication<Application>().filesDir, "uploads_${getSanitizedEmail()}.json")
    }

    fun saveUploads() {
        try {
            val dtoList = uploads.map {
                UploadDto(
                    id = it.id,
                    fileName = it.fileName,
                    fileType = it.fileType,
                    fileSize = it.fileSize,
                    uploadDate = it.uploadDate,
                    uriString = it.uri?.toString()
                )
            }
            getUploadsFile().writeText(gson.toJson(dtoList))
        } catch(e: Exception) {
            Log.e(tag, "Failed to save uploads: ${e.localizedMessage}")
        }
    }

    fun loadUploads() {
        try {
            val file = getUploadsFile()
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<UploadDto>>() {}.type
                val dtoList: List<UploadDto> = gson.fromJson(json, type) ?: emptyList()
                val loadedUploads = dtoList.map { dto ->
                    val isImage = dto.fileType in listOf("PNG", "JPG", "JPEG", "GIF", "WEBP")
                    val isDoc = dto.fileType in listOf("PDF", "DOC", "DOCX", "TXT", "XLS", "XLSX", "PPT", "PPTX")
                    val icon = if (isImage) Icons.Filled.Image 
                               else if (isDoc) Icons.Filled.Description 
                               else Icons.Filled.AttachFile
                    Upload(
                        id = dto.id,
                        fileName = dto.fileName,
                        fileType = dto.fileType,
                        fileSize = dto.fileSize,
                        uploadDate = dto.uploadDate,
                        icon = icon,
                        uri = dto.uriString?.let { android.net.Uri.parse(it) }
                    )
                }
                uploads.clear()
                uploads.addAll(loadedUploads)
            } else {
                uploads.clear()
            }
        } catch(e: Exception) {
            Log.e(tag, "Failed to load uploads: ${e.localizedMessage}")
        }
    }
    
    // Core Notes list
    var notes by mutableStateOf<List<LocationNote>>(emptyList())
        private set

    // Loading flag
    var isLoading by mutableStateOf(false)
        private set

    // User's current location fetched from GPS
    var userLocation by mutableStateOf<Location?>(null)
        private set

    // Selected note for bottom map card preview
    var selectedNote by mutableStateOf<LocationNote?>(null)

    // Location search query
    var searchQuery by mutableStateOf("")

    // Radius filter (in Kilometers)
    var radiusFilter by mutableFloatStateOf(5.0f)

    // Toggle filter by distance
    var isRadiusFilterEnabled by mutableStateOf(false)

    // Temporary states used when adding a new note
    var tempLatitude by mutableStateOf(0.0)
        private set
    var tempLongitude by mutableStateOf(0.0)
        private set
    var tempAddress by mutableStateOf("")
        private set
    var isFetchingLocationDetails by mutableStateOf(false)
        private set

    init {
        fetchNotes()
    }

    // ── ACTIONS ───────────────────────────────────────────────────

    /**
     * Loads location notes and schedules/refreshes geofences.
     */
    fun fetchNotes() {
        loadUploads()
        viewModelScope.launch {
            isLoading = true
            repository.getNotes()
                .onSuccess { list ->
                    notes = list
                    registerAllGeofences(list)
                }
                .onFailure { error ->
                    Log.e(tag, "Failed to load location notes: ${error.localizedMessage}")
                }
            isLoading = false
        }
    }

    /**
     * Registers geofence alerts for all fetched notes.
     */
    private fun registerAllGeofences(notesList: List<LocationNote>) {
        notesList.forEach { note ->
            geofenceManager.addGeofence(
                noteId = note.id.toString(),
                latitude = note.latitude,
                longitude = note.longitude,
                title = "Note Reminder: ${note.title}",
                description = note.description
            )
        }
    }

    /**
     * Saves a new location-based note.
     */
    fun saveLocationNote(
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String,
        isSecured: Boolean = false,
        securityPassword: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            repository.createNote(title, description, latitude, longitude, address, category, colorHex, isSecured, securityPassword)
                .onSuccess { newNote ->
                    // Add geofence immediately
                    geofenceManager.addGeofence(
                        noteId = newNote.id.toString(),
                        latitude = newNote.latitude,
                        longitude = newNote.longitude,
                        title = "Note Reminder: ${newNote.title}",
                        description = newNote.description
                    )
                    fetchNotes() // Reload data
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
                .onFailure { error ->
                    Log.e(tag, "Error saving note: ${error.localizedMessage}")
                }
            isLoading = false
        }
    }

    fun updateLocationNote(
        id: Int,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String,
        isSecured: Boolean = false,
        securityPassword: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            repository.updateNote(id, title, description, latitude, longitude, address, category, colorHex, isSecured, securityPassword)
                .onSuccess { newNote ->
                    // Refresh geofence
                    geofenceManager.removeGeofence(newNote.id.toString())
                    geofenceManager.addGeofence(
                        noteId = newNote.id.toString(),
                        latitude = newNote.latitude,
                        longitude = newNote.longitude,
                        title = "Note Reminder: ${newNote.title}",
                        description = newNote.description
                    )
                    fetchNotes() // Reload data
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
                .onFailure { error ->
                    Log.e(tag, "Error updating note: ${error.localizedMessage}")
                }
            isLoading = false
        }
    }

    /**
     * Deletes a location note.
     */
    fun deleteLocationNote(id: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            geofenceManager.removeGeofence(id.toString())
            repository.deleteNote(id)
                .onSuccess {
                    if (selectedNote?.id == id) {
                        selectedNote = null
                    }
                    fetchNotes()
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
        }
    }

    /**
     * Resolves the user's current GPS location and reverse-geocodes it to a physical address.
     */
    @SuppressLint("MissingPermission")
    fun requestCurrentGPSLocation(onCompleted: (Double, Double, String) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch {
            isFetchingLocationDetails = true
            try {
                // Request current single high accuracy location
                val cancellationToken = CancellationTokenSource().token
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLocation = location
                        tempLatitude = location.latitude
                        tempLongitude = location.longitude

                        // Perform reverse geocoding on a Background thread
                        viewModelScope.launch(Dispatchers.IO) {
                            val address = reverseGeocode(location.latitude, location.longitude)
                            withContext(Dispatchers.Main) {
                                tempAddress = address
                                isFetchingLocationDetails = false
                                onCompleted(location.latitude, location.longitude, address)
                            }
                        }
                    } else {
                        Log.e(tag, "Location is null, using standard fallback coordinates")
                        isFetchingLocationDetails = false
                    }
                }.addOnFailureListener { e ->
                    Log.e(tag, "Failed to get location: ${e.localizedMessage}")
                    isFetchingLocationDetails = false
                }
            } catch (e: SecurityException) {
                Log.e(tag, "Location permissions not granted: ${e.localizedMessage}")
                isFetchingLocationDetails = false
            }
        }
    }

    /**
     * Synchronously resolves latitude & longitude to address. Must run on IO Dispatcher.
     */
    private fun reverseGeocode(lat: Double, lng: Double): String {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addressObj = addresses[0]
                val addressLines = mutableListOf<String>()
                for (i in 0..addressObj.maxAddressLineIndex) {
                    addressLines.add(addressObj.getAddressLine(i))
                }
                addressLines.joinToString(", ")
            } else {
                "Latitude: $lat, Longitude: $lng"
            }
        } catch (e: IOException) {
            Log.e(tag, "Geocoder failed due to network: ${e.localizedMessage}")
            "Latitude: $lat, Longitude: $lng"
        } catch (e: Exception) {
            Log.e(tag, "Geocoder error: ${e.localizedMessage}")
            "Latitude: $lat, Longitude: $lng"
        }
    }

    /**
     * Updates user's location (called from screen updates).
     */
    fun updateUserLocation(location: Location) {
        userLocation = location
    }

    /**
     * Computes distance from user to a note.
     */
    fun getDistanceFromUser(note: LocationNote): Float? {
        val userLoc = userLocation ?: return null
        val results = FloatArray(1)
        Location.distanceBetween(
            userLoc.latitude, userLoc.longitude,
            note.latitude, note.longitude,
            results
        )
        return results[0] // returns distance in meters
    }

    /**
     * Returns a formatted distance string (e.g. "350 m away" or "1.5 km away").
     */
    fun getFormattedDistance(note: LocationNote): String {
        val meters = getDistanceFromUser(note) ?: return "Location unknown"
        return if (meters < 1000) {
            "${meters.toInt()} m away"
        } else {
            String.format(Locale.getDefault(), "%.1f km away", meters / 1000.0)
        }
    }

    /**
     * Gets filtered notes based on search query and proximity radius toggles.
     */
    fun getFilteredNotesList(): List<LocationNote> {
        return notes.filter { note ->
            // Search query filter (matches title, description, category, or address)
            val matchesQuery = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.description.contains(searchQuery, ignoreCase = true) ||
                    note.address.contains(searchQuery, ignoreCase = true) ||
                    note.category.contains(searchQuery, ignoreCase = true)

            // Radius proximity filter
            val matchesRadius = if (isRadiusFilterEnabled) {
                val distanceMeters = getDistanceFromUser(note)
                if (distanceMeters != null) {
                    val distanceKm = distanceMeters / 1000.0
                    distanceKm <= radiusFilter
                } else {
                    false
                }
            } else {
                true
            }

            matchesQuery && matchesRadius
        }
    }
}
