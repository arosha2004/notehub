package com.example.notehub.data.repository

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.notehub.data.remote.RetrofitClient
import com.example.notehub.data.remote.toDomain
import com.example.notehub.data.remote.toDto
import com.example.notehub.domain.model.LocationNote
import com.example.notehub.domain.repository.LocationNotesRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LocationNotesRepositoryImpl — Implements the LocationNotesRepository interface.
 * Uses a robust Online/Offline sync design:
 * 1. Synchronizes with the Laravel JWT backend via Retrofit when online.
 * 2. Caches all notes in a local JSON file (`notes_cache.json`) for instant offline loads.
 * 3. Records any offline creations (with negative IDs), updates, or deletions in a queue (`pending_sync.json`).
 * 4. Automatically plays back the synchronization actions in order once the internet is restored.
 * 5. Since the AWS server database schema only stores basic text fields (title, content/description),
 *    this repository merges the online notes with the local cache to preserve rich properties (coordinates,
 *    address, category, color, and biometrics/password settings) completely client-side!
 */
class LocationNotesRepositoryImpl(private val context: Context) : LocationNotesRepository {

    private val tag = "LocationNotesRepo"
    
    // Cache and Sync files
    private val cacheFile = File(context.filesDir, "notes_cache.json")
    private val syncFile = File(context.filesDir, "pending_sync.json")
    private val gson = Gson()

    // Local memory list
    private val localNotes = mutableListOf<LocationNote>()

    enum class SyncActionType { CREATE, UPDATE, DELETE }

    data class PendingSyncAction(
        val type: SyncActionType,
        val noteId: Int,
        val note: LocationNote? = null
    )

    init {
        loadNotesFromCache()
    }

    private fun prepopulateNotes() {
        localNotes.clear()
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

    private fun saveNotesToCache() {
        try {
            val json = gson.toJson(localNotes)
            cacheFile.writeText(json)
        } catch (e: Exception) {
            Log.e(tag, "Failed to save notes cache: ${e.localizedMessage}")
        }
    }

    private fun loadNotesFromCache() {
        try {
            if (cacheFile.exists()) {
                val json = cacheFile.readText()
                val type = object : TypeToken<List<LocationNote>>() {}.type
                val cached: List<LocationNote> = gson.fromJson(json, type) ?: emptyList()
                localNotes.clear()
                localNotes.addAll(cached)
            } else {
                prepopulateNotes()
                saveNotesToCache()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load notes cache: ${e.localizedMessage}")
            prepopulateNotes()
        }
    }

    private fun loadPendingActions(): MutableList<PendingSyncAction> {
        try {
            if (syncFile.exists()) {
                val json = syncFile.readText()
                val type = object : TypeToken<MutableList<PendingSyncAction>>() {}.type
                return gson.fromJson(json, type) ?: mutableListOf()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load pending actions: ${e.localizedMessage}")
        }
        return mutableListOf()
    }

    private fun savePendingActions(actions: List<PendingSyncAction>) {
        try {
            val json = gson.toJson(actions)
            syncFile.writeText(json)
        } catch (e: Exception) {
            Log.e(tag, "Failed to save pending actions: ${e.localizedMessage}")
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun syncPendingActions() {
        if (!isOnline()) return
        val actions = loadPendingActions()
        if (actions.isEmpty()) return

        Log.d(tag, "Found ${actions.size} pending actions to sync.")
        val remainingActions = mutableListOf<PendingSyncAction>()
        remainingActions.addAll(actions)

        var hasFailed = false
        val iterator = remainingActions.iterator()
        val idMap = mutableMapOf<Int, Int>()

        while (iterator.hasNext() && !hasFailed) {
            val action = iterator.next()
            try {
                when (action.type) {
                    SyncActionType.CREATE -> {
                        val currentNote = action.note ?: continue
                        val noteToCreate = if (idMap.containsKey(currentNote.id)) {
                            currentNote.copy(id = idMap[currentNote.id]!!)
                        } else {
                            currentNote
                        }

                        val apiResponse = RetrofitClient.api.createNote(noteToCreate.toDto())
                        if (apiResponse.success && apiResponse.data != null) {
                            val savedNote = apiResponse.data.note.toDomain()
                            val finalNote = savedNote.copy(
                                latitude = noteToCreate.latitude,
                                longitude = noteToCreate.longitude,
                                address = noteToCreate.address,
                                category = noteToCreate.category,
                                colorHex = noteToCreate.colorHex,
                                isSecured = noteToCreate.isSecured,
                                securityPassword = noteToCreate.securityPassword
                            )
                            idMap[currentNote.id] = finalNote.id

                            val index = localNotes.indexOfFirst { it.id == currentNote.id }
                            if (index != -1) {
                                localNotes[index] = finalNote
                            }
                            Log.d(tag, "Sync CREATE success. Mapped temp ID ${currentNote.id} to ${finalNote.id}")
                            iterator.remove()
                        } else {
                            hasFailed = true
                        }
                    }
                    SyncActionType.UPDATE -> {
                        val currentNote = action.note ?: continue
                        val noteToUpdate = if (idMap.containsKey(currentNote.id)) {
                            currentNote.copy(id = idMap[currentNote.id]!!)
                        } else {
                            currentNote
                        }

                        val apiResponse = RetrofitClient.api.updateNote(noteToUpdate.toDto())
                        if (apiResponse.success && apiResponse.data != null) {
                            val savedNote = apiResponse.data.note.toDomain()
                            val finalNote = savedNote.copy(
                                latitude = noteToUpdate.latitude,
                                longitude = noteToUpdate.longitude,
                                address = noteToUpdate.address,
                                category = noteToUpdate.category,
                                colorHex = noteToUpdate.colorHex,
                                isSecured = noteToUpdate.isSecured,
                                securityPassword = noteToUpdate.securityPassword
                            )
                            val index = localNotes.indexOfFirst { it.id == noteToUpdate.id }
                            if (index != -1) {
                                localNotes[index] = finalNote
                            }
                            Log.d(tag, "Sync UPDATE success for ID ${noteToUpdate.id}")
                            iterator.remove()
                        } else {
                            hasFailed = true
                        }
                    }
                    SyncActionType.DELETE -> {
                        val targetId = if (idMap.containsKey(action.noteId)) {
                            idMap[action.noteId]!!
                        } else {
                            action.noteId
                        }

                        val response = RetrofitClient.api.deleteNote(targetId)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d(tag, "Sync DELETE success for ID $targetId")
                            iterator.remove()
                        } else {
                            hasFailed = true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Sync action failed: ${e.localizedMessage}")
                hasFailed = true
            }
        }

        savePendingActions(remainingActions)
        saveNotesToCache()
    }

    override suspend fun getNotes(): Result<List<LocationNote>> = withContext(Dispatchers.IO) {
        if (localNotes.isEmpty()) {
            loadNotesFromCache()
        }

        if (isOnline()) {
            syncPendingActions()

            try {
                Log.d(tag, "Attempting to fetch notes from AWS backend...")
                val apiResponse = RetrofitClient.api.getNotes()
                if (apiResponse.success && apiResponse.data != null) {
                    val domainNotes = apiResponse.data.notes.map { dto ->
                        val domain = dto.toDomain()
                        val cached = localNotes.find { it.id == domain.id }
                        if (cached != null) {
                            domain.copy(
                                latitude = cached.latitude,
                                longitude = cached.longitude,
                                address = cached.address,
                                category = cached.category,
                                colorHex = cached.colorHex,
                                isSecured = cached.isSecured,
                                securityPassword = cached.securityPassword
                            )
                        } else {
                            domain
                        }
                    }
                    
                    // Preserve offline-created notes (ID < 0)
                    val offlineNotes = localNotes.filter { it.id < 0 }

                    localNotes.clear()
                    localNotes.addAll(offlineNotes)
                    localNotes.addAll(domainNotes)
                    saveNotesToCache()

                    Log.d(tag, "Successfully loaded ${domainNotes.size} notes from API.")
                    Result.success(ArrayList(localNotes))
                } else {
                    Log.w(tag, "API returned failure: ${apiResponse.message}")
                    Result.success(ArrayList(localNotes))
                }
            } catch (e: Exception) {
                Log.w(tag, "Remote backend unreachable. Falling back to local cache. Error: ${e.localizedMessage}")
                Result.success(ArrayList(localNotes))
            }
        } else {
            Log.d(tag, "Device is offline. Returning local cache.")
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
        colorHex: String,
        isSecured: Boolean,
        securityPassword: String?
    ): Result<LocationNote> = withContext(Dispatchers.IO) {
        if (localNotes.isEmpty()) {
            loadNotesFromCache()
        }

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newId = -Math.abs(System.currentTimeMillis().toInt())

        val newNote = LocationNote(
            id = newId,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            address = address,
            date = dateString,
            category = category,
            colorHex = colorHex,
            isSecured = isSecured,
            securityPassword = securityPassword
        )

        if (isOnline()) {
            syncPendingActions()
            try {
                Log.d(tag, "Attempting to sync new note to AWS backend...")
                val apiResponse = RetrofitClient.api.createNote(newNote.toDto())
                if (apiResponse.success && apiResponse.data != null) {
                    val savedNote = apiResponse.data.note.toDomain()
                    val finalNote = savedNote.copy(
                        latitude = latitude,
                        longitude = longitude,
                        address = address,
                        category = category,
                        colorHex = colorHex,
                        isSecured = isSecured,
                        securityPassword = securityPassword
                    )
                    localNotes.add(0, finalNote)
                    saveNotesToCache()
                    Log.d(tag, "Successfully synced note to API with ID: ${finalNote.id}")
                    return@withContext Result.success(finalNote)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to connect to backend, saving note offline. Error: ${e.localizedMessage}")
            }
        }

        localNotes.add(0, newNote)
        saveNotesToCache()

        val actions = loadPendingActions()
        actions.add(PendingSyncAction(SyncActionType.CREATE, newId, newNote))
        savePendingActions(actions)

        Result.success(newNote)
    }

    override suspend fun updateNote(
        id: Int,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        category: String,
        colorHex: String,
        isSecured: Boolean,
        securityPassword: String?
    ): Result<LocationNote> = withContext(Dispatchers.IO) {
        if (localNotes.isEmpty()) {
            loadNotesFromCache()
        }

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedNote = LocationNote(
            id = id,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            address = address,
            date = dateString,
            category = category,
            colorHex = colorHex,
            isSecured = isSecured,
            securityPassword = securityPassword
        )

        localNotes.removeAll { it.id == id }
        localNotes.add(0, updatedNote)
        saveNotesToCache()

        if (isOnline() && id > 0) {
            syncPendingActions()
            try {
                Log.d(tag, "Attempting to sync updated note to AWS backend...")
                val apiResponse = RetrofitClient.api.updateNote(updatedNote.toDto())
                if (apiResponse.success && apiResponse.data != null) {
                    val savedNote = apiResponse.data.note.toDomain()
                    val finalNote = savedNote.copy(
                        latitude = latitude,
                        longitude = longitude,
                        address = address,
                        category = category,
                        colorHex = colorHex,
                        isSecured = isSecured,
                        securityPassword = securityPassword
                    )
                    localNotes.removeAll { it.id == id }
                    localNotes.add(0, finalNote)
                    saveNotesToCache()
                    Log.d(tag, "Successfully synced updated note to API with ID: ${finalNote.id}")
                    return@withContext Result.success(finalNote)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to connect to backend, queueing update offline. Error: ${e.localizedMessage}")
            }
        }

        val actions = loadPendingActions()
        if (id < 0) {
            val createIndex = actions.indexOfFirst { it.type == SyncActionType.CREATE && it.noteId == id }
            if (createIndex != -1) {
                actions[createIndex] = actions[createIndex].copy(note = updatedNote)
            } else {
                actions.add(PendingSyncAction(SyncActionType.CREATE, id, updatedNote))
            }
        } else {
            actions.removeAll { it.noteId == id && (it.type == SyncActionType.UPDATE || it.type == SyncActionType.DELETE) }
            actions.add(PendingSyncAction(SyncActionType.UPDATE, id, updatedNote))
        }
        savePendingActions(actions)

        Result.success(updatedNote)
    }

    override suspend fun deleteNote(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (localNotes.isEmpty()) {
            loadNotesFromCache()
        }

        localNotes.removeAll { it.id == id }
        saveNotesToCache()

        if (isOnline() && id > 0) {
            syncPendingActions()
            try {
                Log.d(tag, "Attempting to delete note $id from AWS backend...")
                val response = RetrofitClient.api.deleteNote(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(tag, "Successfully deleted note $id from API.")
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to connect to backend, queueing delete offline. Error: ${e.localizedMessage}")
            }
        }

        val actions = loadPendingActions()
        if (id < 0) {
            actions.removeAll { it.noteId == id }
        } else {
            actions.removeAll { it.noteId == id }
            actions.add(PendingSyncAction(SyncActionType.DELETE, id))
        }
        savePendingActions(actions)

        Result.success(Unit)
    }

    override suspend fun getNearbyNotes(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Result<List<LocationNote>> = withContext(Dispatchers.IO) {
        val notesResult = getNotes()
        notesResult.map { list ->
            list.filter { note ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    latitude, longitude,
                    note.latitude, note.longitude,
                    results
                )
                val distanceInKm = results[0] / 1000.0
                distanceInKm <= radiusInKm
            }
        }
    }
}
