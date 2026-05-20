package com.example.notehub.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * GeofenceManager — Wrapper around Play Services GeofencingClient to schedule
 * and clear background location-based note reminders.
 */
class GeofenceManager(private val context: Context) {

    private val tag = "GeofenceManager"
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    /**
     * Schedules a geofence around a coordinate.
     * Triggers the GeofenceBroadcastReceiver when the user enters the circular boundary.
     *
     * @param noteId Unique ID of the note (used as the geofence request ID)
     * @param latitude Target Latitude
     * @param longitude Target Longitude
     * @param title Title of the note to display in the notification
     * @param description Brief snippet of the note content
     * @param radiusInMeters Circular trigger range (defaults to 150m)
     */
    @SuppressLint("MissingPermission")
    fun addGeofence(
        noteId: String,
        latitude: Double,
        longitude: Double,
        title: String,
        description: String,
        radiusInMeters: Float = 150f
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(noteId)
            .setCircularRegion(latitude, longitude, radiusInMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            putExtra("note_title", title)
            putExtra("note_desc", description)
        }

        // Geofencing PendingIntents MUST be MUTABLE so Android can append location data when triggered
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            intent,
            flags
        )

        try {
            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Log.i(tag, "Successfully scheduled geofence for Note: $noteId ($latitude, $longitude)")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Failed to schedule geofence for Note: $noteId. Error: ${e.localizedMessage}")
                }
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException: Location permissions missing! ${e.localizedMessage}")
        }
    }

    /**
     * Unregisters a geofence by its note ID request.
     */
    fun removeGeofence(noteId: String) {
        geofencingClient.removeGeofences(listOf(noteId))
            .addOnSuccessListener {
                Log.i(tag, "Successfully removed geofence for Note: $noteId")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to remove geofence for Note: $noteId. Error: ${e.localizedMessage}")
            }
    }
}
