package com.example.notehub.domain.model

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Upload — Domain model representing an uploaded file from the device storage.
 */
data class Upload(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val uploadDate: String,
    val icon: ImageVector,
    val uri: Uri? = null
)
