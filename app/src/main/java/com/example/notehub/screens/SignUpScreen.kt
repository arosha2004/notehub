package com.example.notehub.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
package com.example.notehub.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.R
import com.example.notehub.data.AuthResult
import com.example.notehub.data.AuthService
import com.example.notehub.ui.components.NoteHubTextField
import com.example.notehub.ui.theme.*
import com.example.notehub.utils.NetworkMonitor
import kotlinx.coroutines.launch

// Premium registration screen
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // STATE
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Live network state
    val isOnline by NetworkMonitor.observeNetwork()
        .collectAsState(initial = NetworkMonitor.isOnline())
    val statusColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF59E0B)
    val statusIcon  = if (isOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff
    val statusLabel = if (isOnline) "Online" else "Offline"

    // UI LAYOUT
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Network status pill (top-right)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp),
            shape = RoundedCornerShape(50),
            color = statusColor.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(6.dp))
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO SECTION
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Join our community today",
                fontSize = 15.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // FORM CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // OFFLINE WARNING BANNER
                    AnimatedVisibility(
                        visible = !isOnline,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.WifiOff, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("No Internet Connection", color = Color(0xFFF59E0B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Connect to the internet to create an account.", color = Color(0xFFF59E0B).copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // ERROR ALERT
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                color = ErrorRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = msg, color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // FULL NAME
                    NoteHubTextField(
                        value = fullName,
                        onValueChange = { 
                            fullName = it
                            errorMessage = null
                        },
                        label = "Full Name",
                        placeholder = "John Doe",
                        leadingIcon = Icons.Filled.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // EMAIL
                    NoteHubTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            errorMessage = null
                        },
                        label = "Email Address",
                        placeholder = "john@example.com",
                        leadingIcon = Icons.Filled.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD
                    NoteHubTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = null
                        },
                        label = "Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Filled.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = IconSecondary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CONFIRM PASSWORD
                    NoteHubTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Confirm Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Filled.LockClock,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = IconSecondary
                                )
                            }
                        },
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                        errorMessage = if (confirmPassword.isNotEmpty() && password != confirmPassword) "Passwords do not match" else null
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // SIGN UP BUTTON (disabled when offline)
                    Button(
                        onClick = {
                            if (!isOnline) {
                                errorMessage = "You are offline. Connect to the internet to create an account."
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                when (val result = AuthService.signUp(fullName, email, password)) {
                                    is AuthResult.Success -> onSignUpSuccess()
                                    is AuthResult.Error -> {
                                        errorMessage = result.message
                                        isLoading = false
                                    }
                                    else -> {}
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) PrimaryBlue else Color(0xFF9E9E9E),
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = if (isOnline) "Create Account" else "No Internet — Cannot Register",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // FOOTER
            Row(
                modifier = Modifier.padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
