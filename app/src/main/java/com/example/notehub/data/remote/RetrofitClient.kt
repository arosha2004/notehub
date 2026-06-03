package com.example.notehub.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient — Initializes Retrofit network connection targeting the JWT backend.
 * Integrates logging, token injection, and clean AWS path/payload translation.
 */
object RetrofitClient {

    // Target AWS server API base URL
    private const val BASE_URL = "http://44-197-113-192.nip.io/api/"

    private fun rewriteJsonRequestBody(body: okhttp3.RequestBody?, transform: (String) -> String): okhttp3.RequestBody? {
        if (body == null) return null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val originalString = buffer.readUtf8()
            val newString = transform(originalString)
            okhttp3.RequestBody.create(body.contentType(), newString)
        } catch (e: Exception) {
            Log.e("RetrofitClient", "Failed to rewrite JSON request body", e)
            body
        }
    }

    /**
     * Interceptor that handles both local PHP backend cookie sessions and AWS Sanctum Bearer tokens,
     * translating path layouts and payload formats dynamically.
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val isAws = originalUrl.host == "44-197-113-192.nip.io"

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        // 1. Inject Token
        val token = TokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            if (isAws) {
                if (!token.contains("PHPSESSID")) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            } else {
                requestBuilder.header("Cookie", token)
            }
        }

        // 2. Request URL and Body translation for AWS Laravel backend compatibility
        var modifiedRequest = requestBuilder.build()
        var rewrittenPath = ""

        if (isAws) {
            val pathSegments = originalUrl.pathSegments
            val pathString = pathSegments.joinToString("/")
            val newUrlBuilder = originalUrl.newBuilder()
            
            if (pathString.contains("auth/login.php")) {
                rewrittenPath = "login"
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("login")
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).post(originalRequest.body!!).build()
            } else if (pathString.contains("auth/register.php")) {
                rewrittenPath = "register"
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("register")
                
                val newBody = rewriteJsonRequestBody(originalRequest.body) { bodyStr ->
                    try {
                        val json = JSONObject(bodyStr)
                        if (json.has("full_name")) {
                            json.put("name", json.getString("full_name"))
                            json.remove("full_name")
                        }
                        if (!json.has("username") && json.has("name")) {
                            json.put("username", json.getString("name"))
                        }
                        if (json.has("password") && !json.has("password_confirmation")) {
                            json.put("password_confirmation", json.getString("password"))
                        }
                        json.toString()
                    } catch (e: Exception) {
                        bodyStr
                    }
                }
                if (newBody != null) {
                    requestBuilder.post(newBody)
                }
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).build()
            } else if (pathString.contains("notes/list.php")) {
                rewrittenPath = "list"
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("notes")
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).get().build()
            } else if (pathString.contains("notes/create.php")) {
                rewrittenPath = "create"
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("notes")
                
                val newBody = rewriteJsonRequestBody(originalRequest.body) { bodyStr ->
                    try {
                        val json = JSONObject(bodyStr)
                        if (json.has("description")) {
                            json.put("content", json.getString("description"))
                            json.remove("description")
                        }
                        json.toString()
                    } catch (e: Exception) {
                        bodyStr
                    }
                }
                if (newBody != null) {
                    requestBuilder.post(newBody)
                }
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).build()
            } else if (pathString.contains("notes/update.php")) {
                rewrittenPath = "update"
                var noteId = 0
                val newBody = rewriteJsonRequestBody(originalRequest.body) { bodyStr ->
                    try {
                        val json = JSONObject(bodyStr)
                        noteId = json.optInt("id", 0)
                        if (json.has("description")) {
                            json.put("content", json.getString("description"))
                            json.remove("description")
                        }
                        json.toString()
                    } catch (e: Exception) {
                        bodyStr
                    }
                }
                
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("notes")
                newUrlBuilder.addPathSegment(noteId.toString())
                
                if (newBody != null) {
                    requestBuilder.put(newBody)
                }
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).build()
            } else if (pathString.contains("notes/delete.php")) {
                rewrittenPath = "delete"
                val noteId = originalUrl.queryParameter("note_id") ?: "0"
                
                newUrlBuilder.removePathSegment(pathSegments.size - 1)
                newUrlBuilder.removePathSegment(pathSegments.size - 2)
                newUrlBuilder.addPathSegment("notes")
                newUrlBuilder.addPathSegment(noteId)
                newUrlBuilder.removeAllQueryParameters("note_id")
                
                modifiedRequest = requestBuilder.url(newUrlBuilder.build()).delete().build()
            }
        }

        val response = chain.proceed(modifiedRequest)

        // 3. Response payload translation for AWS Laravel backend outputs
        if (isAws) {
            val responseCode = response.code
            val contentType = response.body?.contentType()
            val originalBodyStr = response.body?.string() ?: ""
            var rewrittenBodyStr = originalBodyStr
            var rewrittenCode = responseCode

            if (response.isSuccessful || responseCode == 422 || responseCode == 201 || responseCode == 204) {
                try {
                    when (rewrittenPath) {
                        "login", "register" -> {
                            if (responseCode == 200 || responseCode == 201) {
                                val json = JSONObject(originalBodyStr)
                                val accessToken = json.optString("access_token", "")
                                if (accessToken.isNotEmpty()) {
                                    TokenManager.saveToken(accessToken)
                                }
                                val userObj = json.optJSONObject("user")
                                val userId = userObj?.optInt("id", 0) ?: 0
                                val userEmail = userObj?.optString("email", "") ?: ""
                                val userName = userObj?.optString("name", "") ?: userObj?.optString("first_name", "") ?: ""

                                if (rewrittenPath == "login") {
                                    val newUserJson = JSONObject()
                                    newUserJson.put("id", userId)
                                    newUserJson.put("username", userName)
                                    newUserJson.put("email", userEmail)
                                    newUserJson.put("role", "user")
                                    newUserJson.put("plan", "Premium")

                                    val dataJson = JSONObject()
                                    dataJson.put("user", newUserJson)
                                    dataJson.put("redirect", "")

                                    val wrapper = JSONObject()
                                    wrapper.put("success", true)
                                    wrapper.put("message", "Login successful")
                                    wrapper.put("data", dataJson)

                                    rewrittenBodyStr = wrapper.toString()
                                } else {
                                    val wrapper = JSONObject()
                                    wrapper.put("success", true)
                                    wrapper.put("message", "Registration successful")
                                    
                                    val dataJson = JSONObject()
                                    dataJson.put("user_id", userId)
                                    dataJson.put("redirect", "")
                                    
                                    wrapper.put("data", dataJson)

                                    rewrittenBodyStr = wrapper.toString()
                                }
                            }
                        }
                        "list" -> {
                            if (responseCode == 200) {
                                val json = JSONObject(originalBodyStr)
                                val dataArray = json.optJSONArray("data") ?: JSONArray()
                                val total = json.optInt("total", dataArray.length())
                                
                                val notesList = JSONArray()
                                for (i in 0 until dataArray.length()) {
                                    val noteObj = dataArray.getJSONObject(i)
                                    if (noteObj.has("content")) {
                                        noteObj.put("description", noteObj.optString("content", ""))
                                        noteObj.remove("content")
                                    }
                                    notesList.put(noteObj)
                                }

                                val dataJson = JSONObject()
                                dataJson.put("notes", notesList)
                                dataJson.put("count", total)

                                val wrapper = JSONObject()
                                wrapper.put("success", true)
                                wrapper.put("message", "Notes retrieved successfully")
                                wrapper.put("data", dataJson)

                                rewrittenBodyStr = wrapper.toString()
                            }
                        }
                        "create", "update" -> {
                            if (responseCode == 200 || responseCode == 201) {
                                val noteObj = JSONObject(originalBodyStr)
                                if (noteObj.has("content")) {
                                    noteObj.put("description", noteObj.optString("content", ""))
                                    noteObj.remove("content")
                                }

                                val dataJson = JSONObject()
                                dataJson.put("note", noteObj)

                                val wrapper = JSONObject()
                                wrapper.put("success", true)
                                wrapper.put("message", "Note saved successfully")
                                wrapper.put("data", dataJson)

                                rewrittenBodyStr = wrapper.toString()
                                rewrittenCode = 200
                            }
                        }
                        "delete" -> {
                            if (responseCode == 200 || responseCode == 204) {
                                val wrapper = JSONObject()
                                wrapper.put("success", true)
                                wrapper.put("message", "Note deleted successfully")
                                wrapper.put("data", JSONObject.NULL)

                                rewrittenBodyStr = wrapper.toString()
                                rewrittenCode = 200
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RetrofitClient", "Failed to rewrite AWS response body", e)
                }
            }

            val newBody = rewrittenBodyStr.toResponseBody(contentType)
            response.newBuilder()
                .code(rewrittenCode)
                .body(newBody)
                .build()
        } else {
            val cookies = response.headers("Set-Cookie")
            for (cookie in cookies) {
                if (cookie.contains("PHPSESSID")) {
                    val sessionCookie = cookie.split(";")[0]
                    TokenManager.saveToken(sessionCookie)
                }
            }
            response
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: LocationNotesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationNotesApi::class.java)
    }
}
