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

// Initializes network connection to AWS Laravel backend
object RetrofitClient {

    fun getBaseUrl(): String {
        // Always target the AWS backend — XAMPP local is no longer used
        return DataSettings.AWS_BASE_URL
    }

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

    // Interceptor for headers and AWS payload translations
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        // isAws is always true when online (we always hit the AWS Laravel backend)
        val isAws = DataSettings.isAwsMode()

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

            // Handle all responses — success, validation errors (422), auth errors (401), etc.
            if (true) {
                try {
                    when (rewrittenPath) {
                        "login", "register" -> {
                            if (responseCode == 200 || responseCode == 201) {
                                // SUCCESS: parse Laravel response and rewrite to our ApiResponse format
                                val json = JSONObject(originalBodyStr)
                                val accessToken = json.optString("access_token", "")
                                if (accessToken.isNotEmpty()) {
                                    TokenManager.saveToken(accessToken)
                                }
                                val userObj = json.optJSONObject("user")
                                val userId = userObj?.optInt("id", 0) ?: 0
                                val userEmail = userObj?.optString("email", "") ?: ""
                                val userName = userObj?.optString("name", "") ?: userObj?.optString("first_name", "") ?: ""

                                // Save user identity — detects if a different account just logged in
                                val isAccountSwitch = TokenManager.saveUser(userEmail, userName, userId)
                                if (isAccountSwitch) {
                                    Log.w("RetrofitClient", "Account switch detected! New user: $userEmail")
                                } else {
                                    Log.d("RetrofitClient", "Auth success for user: $userEmail")
                                }

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
                                rewrittenCode = 200

                            } else {
                                // ERROR (401 wrong password, 422 validation, 500, etc.)
                                // Parse Laravel's error body and convert to a clean success:false response
                                // so Retrofit can deserialize it and AuthService shows a readable message.
                                val errorMessage = try {
                                    val json = JSONObject(originalBodyStr)
                                    when {
                                        // Laravel validation errors: {"message":"...","errors":{"field":["msg"]}}
                                        json.has("errors") -> {
                                            val errors = json.optJSONObject("errors")
                                            val firstKey = errors?.keys()?.next()
                                            val firstArr = if (firstKey != null) errors?.optJSONArray(firstKey) else null
                                            firstArr?.optString(0)
                                                ?: json.optString("message", "Invalid credentials")
                                        }
                                        // Simple message field
                                        json.has("message") -> json.optString("message", "Login failed")
                                        // Fallback
                                        else -> when (responseCode) {
                                            401 -> "Invalid email or password"
                                            422 -> "Please check your email and password"
                                            429 -> "Too many attempts. Please try again later"
                                            500 -> "Server error. Please try again"
                                            else -> "Login failed (error $responseCode)"
                                        }
                                    }
                                } catch (parseEx: Exception) {
                                    when (responseCode) {
                                        401 -> "Invalid email or password"
                                        422 -> "Please check your email and password"
                                        else -> "Login failed. Please try again"
                                    }
                                }

                                // Rewrite as success:false with HTTP 200 so Retrofit can parse it
                                val wrapper = JSONObject()
                                wrapper.put("success", false)
                                wrapper.put("message", errorMessage)
                                wrapper.put("data", JSONObject.NULL)
                                rewrittenBodyStr = wrapper.toString()
                                rewrittenCode = 200  // Return 200 so Retrofit doesn't throw HttpException
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
                        else -> {
                            // Global fallback: any unhandled error response → clean success:false
                            if (!response.isSuccessful && responseCode != 204) {
                                try {
                                    val json = JSONObject(originalBodyStr)
                                    val msg = when {
                                        json.has("errors") -> {
                                            val errors = json.optJSONObject("errors")
                                            val firstKey = errors?.keys()?.next()
                                            errors?.optJSONArray(firstKey)?.optString(0)
                                                ?: json.optString("message", "Request failed")
                                        }
                                        json.has("message") -> json.optString("message", "Request failed")
                                        else -> "Request failed (error $responseCode)"
                                    }
                                    val wrapper = JSONObject()
                                    wrapper.put("success", false)
                                    wrapper.put("message", msg)
                                    wrapper.put("data", JSONObject.NULL)
                                    rewrittenBodyStr = wrapper.toString()
                                    rewrittenCode = 200
                                } catch (fe: Exception) {
                                    Log.e("RetrofitClient", "Failed to parse error body", fe)
                                }
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

    private var cachedApi: LocationNotesApi? = null
    private var lastUsedUrl: String? = null

    val api: LocationNotesApi
        get() {
            val currentUrl = getBaseUrl()
            if (cachedApi == null || lastUsedUrl != currentUrl) {
                lastUsedUrl = currentUrl
                cachedApi = Retrofit.Builder()
                    .baseUrl(currentUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(LocationNotesApi::class.java)
            }
            return cachedApi!!
        }
}
