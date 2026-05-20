package com.example.notehub.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * LocationHelper — Utility class to fetch the user's current physical location.
 * Uses Google Play Services Fused Location Provider.
 */
class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationName(): String? {
        return try {
            // Fetch the coordinates (Lat/Lng)
            // Note: Priority.PRIORITY_HIGH_ACCURACY requires play-services-location:21.0.0+
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

            // Convert coordinates to a readable address (City, Country)
            location?.let {
                getAddressFromLocation(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Internal helper to convert coordinates to an address string.
     * Uses suspendCoroutine to support the new callback-based Geocoder API on Android 13+.
     */
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? = suspendCoroutine { continuation ->
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ (Modern way)
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val result = if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        "${address.locality ?: address.subAdminArea ?: "Unknown Location"}, ${address.countryName}"
                    } else {
                        "Unknown Location"
                    }
                    continuation.resume(result)
                }
            } else {
                // Older Android versions (Legacy way)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val result = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    "${address.locality ?: address.subAdminArea ?: "Unknown Location"}, ${address.countryName}"
                } else {
                    "Unknown Location"
                }
                continuation.resume(result)
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}
