package com.example.notehub.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * LocationNotesApi — Retrofit contract mapping endpoints on the Laravel JWT backend.
 */
interface LocationNotesApi {

    @GET("notes/list.php")
    suspend fun getNotes(): ApiResponse<NotesListResponse>

    @POST("notes/create.php")
    suspend fun createNote(@Body note: LocationNoteDto): ApiResponse<NoteResponse>

    @POST("notes/delete.php")
    suspend fun deleteNote(@Query("note_id") id: Int): Response<ApiResponse<Unit>>

    @POST("notes/update.php")
    suspend fun updateNote(@Body note: LocationNoteDto): ApiResponse<NoteResponse>

    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterResponse>
}

// ── RESPONSE WRAPPERS & DATA TRANSFER OBJECTS ───────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class NotesListResponse(
    val notes: List<LocationNoteDto>,
    val count: Int
)

data class NoteResponse(
    val note: LocationNoteDto
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val plan: String
)

data class LoginResponse(
    val user: UserDto,
    val redirect: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val full_name: String
)

data class RegisterResponse(
    val user_id: Int,
    val redirect: String
)
