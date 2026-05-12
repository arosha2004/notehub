package com.example.notehub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*

/**
 * A custom styled OutlinedTextField with support for error messages and premium styling.
 */
@Composable
fun NoteHubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null, tint = if (isError) ErrorRed else PrimaryBlue) }
            },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderMedium,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = TextSecondary,
                cursorColor = PrimaryBlue,
                errorBorderColor = ErrorRed,
                errorLabelColor = ErrorRed,
                errorCursorColor = ErrorRed
            )
        )
        
        // Show error message if present
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
