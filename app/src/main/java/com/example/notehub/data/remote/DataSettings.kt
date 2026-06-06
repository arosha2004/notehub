package com.example.notehub.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.notehub.utils.NetworkMonitor

/**
 * DataSettings — Manages the app's data source configuration.
 *
 * Online/offline is detected automatically via [NetworkMonitor].
 * - ONLINE  → always routes to SSP AWS backend (http://44-197-113-192.nip.io)
 * - OFFLINE → operates in local JSON cache mode (no network calls)
 *
 * Users never need to configure this manually.
 */
object DataSettings {
    private const val PREFS_NAME = "data_settings_prefs"
    private const val KEY_API_SOURCE = "api_source"

    // AWS endpoint — the primary online backend
    const val AWS_BASE_URL = "http://44-197-113-192.nip.io/api/"

    enum class ApiSource(val displayName: String) {
        SSP_API_LOCAL("SSP API (Local XAMPP)"),
        SSP_API_AWS("SSP API (AWS Cloud)"),
        PUBLIC_API("Public API (JSONPlaceholder)"),
        EXTERNAL_JSON_URL("External JSON File (GitHub)"),
        OFFLINE_ONLY("Offline Mode (Local JSON Cache)")
    }

    private var prefs: SharedPreferences? = null

    // Stored preference (used as a fallback / for advanced users)
    var apiSource: ApiSource = ApiSource.SSP_API_AWS
        set(value) {
            field = value
            prefs?.edit()?.putString(KEY_API_SOURCE, value.name)?.apply()
        }

    fun init(context: Context) {
        // Ensure NetworkMonitor is ready first
        NetworkMonitor.init(context)
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Always default to AWS — ignore any old LOCAL preference saved before this update
        val saved = prefs?.getString(KEY_API_SOURCE, ApiSource.SSP_API_AWS.name)
        apiSource = try {
            val src = ApiSource.valueOf(saved ?: ApiSource.SSP_API_AWS.name)
            // Migrate: if old preference was LOCAL, upgrade to AWS
            if (src == ApiSource.SSP_API_LOCAL) ApiSource.SSP_API_AWS else src
        } catch (e: Exception) {
            ApiSource.SSP_API_AWS
        }
    }

    /**
     * The effective API source resolved at runtime:
     * - If online  → SSP_API_AWS (regardless of stored setting)
     * - If offline → OFFLINE_ONLY
     */
    fun effectiveSource(): ApiSource {
        return if (NetworkMonitor.isOnline()) ApiSource.SSP_API_AWS else ApiSource.OFFLINE_ONLY
    }

    /**
     * Returns true when there is no internet — the app should work in
     * read-only offline cache mode and skip all network calls.
     */
    fun isOfflineMode(): Boolean = !NetworkMonitor.isOnline()

    /**
     * Returns true when the app is online and targeting the AWS backend.
     * Used by the Retrofit interceptor to apply Laravel-compatible path rewriting.
     */
    fun isAwsMode(): Boolean = NetworkMonitor.isOnline()
}
