package com.example.notehub.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient — Initializes Retrofit network connection targeting the Laravel JWT backend.
 * Integrates logging and injects Authorization Bearer tokens automatically.
 */
object RetrofitClient {

    // Default base URL for standard Android emulator accessing localhost on the development machine.
    // Replace with staging/production server IP if deploying physically.
    private const val BASE_URL = "http://10.0.2.2/NOTEKEEPER/api/"

    /**
     * Interceptor that fetches PHP session cookies from TokenManager and adds them to headers,
     * and captures Set-Cookie headers from server responses.
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = TokenManager.getToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Cookie", token)
        }

        val response = chain.proceed(requestBuilder.build())

        // Extract and cache PHPSESSID cookie from Response Headers
        val cookies = response.headers("Set-Cookie")
        for (cookie in cookies) {
            if (cookie.contains("PHPSESSID")) {
                val sessionCookie = cookie.split(";")[0]
                TokenManager.saveToken(sessionCookie)
            }
        }

        response
    }

    /**
     * Set up network client configurations (timeouts + interceptors).
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    /**
     * Instantiate Retrofit service.
     */
    val api: LocationNotesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationNotesApi::class.java)
    }
}
