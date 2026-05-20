package com.example.notehub.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.notehub.MainActivity
import com.example.notehub.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * GeofenceBroadcastReceiver — Catches background geofence transition alerts from Google Play Services.
 * Triggers a push notification when the user walks/drives into a note's location.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val tag = "GeofenceReceiver"
    private val channelId = "location_notes_reminders"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received geofence broadcast event!")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(tag, "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(tag, "Geofence event error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        // We trigger alerts when the user ENTERS a note's radius
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: emptyList()
            Log.d(tag, "Triggered geofences count: ${triggeringGeofences.size}")

            for (geofence in triggeringGeofences) {
                val requestId = geofence.requestId // This stores the Note ID or a custom identifier
                
                // Fetch info passed through the Intent extras (saved when registering geofence)
                val noteTitle = intent.getStringExtra("note_title") ?: "Nearby Location Note!"
                val noteDescription = intent.getStringExtra("note_desc") ?: "You are near one of your saved notes."

                Log.d(tag, "Entering Geofence for Note ID: $requestId, Title: $noteTitle")
                sendNotification(context, requestId.hashCode(), noteTitle, noteDescription)
            }
        }
    }

    /**
     * Creates and triggers a system tray notification.
     */
    private fun sendNotification(context: Context, id: Int, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android Oreo (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NoteHub Location Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you of notes when you are near their saved coordinates."
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deep-link to open Main Activity
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification card
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map) // Default system map icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))

        notificationManager.notify(id, builder.build())
    }
}
