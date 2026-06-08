package com.example.notehub.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

// Smart authentication screen with automatic offline fallback
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Live network state
    val isOnline by NetworkMonitor.observeNetwork()
        .collectAsState(initial = NetworkMonitor.isOnline())

    // Online → normal green badge; Offline → amber badge + helper text
    val statusColor  = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF59E0B)
    val statusLabel  = if (isOnline) "Online"         else "Offline"
    val statusIcon   = if (isOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff

    // The email of the last successfully logged-in user (for offline mode)
    val cachedEmail = com.example.notehub.data.remote.TokenManager.getLoggedInEmail()

    // When switching to offline, pre-fill the email with the cached account
    LaunchedEffect(isOnline) {
        if (!isOnline && !cachedEmail.isNullOrEmpty() && email.isBlank()) {
            email = cachedEmail
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            // Network status pill (top-right corner)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 20.dp),
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.12f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LOGO
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    shadowElevation = 12.dp
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
                            contentDescription = "NoteHub Logo",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = if (isOnline) "Sign in to access your notes"
                           else "You're offline — tap Sign In to continue with cached notes",
                    fontSize = 14.sp,
                    color = if (isOnline) TextSecondary else Color(0xFFF59E0B),
                    modifier = Modifier.padding(top = 8.dp, bottom = 36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // LOGIN CARD
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

                        // OFFLINE INFO BANNER
                        AnimatedVisibility(
                            visible = !isOnline,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                                // Part 1: No internet warning
                                Surface(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.WifiOff,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "No Internet Connection",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "You can still access your cached notes offline.",
                                                color = Color(0xFFF59E0B).copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Part 2: Show which account can be used offline
                                if (!cachedEmail.isNullOrEmpty()) {
                                    Surface(
                                        color = ErrorRed.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.AccountCircle,
                                                contentDescription = null,
                                                tint = ErrorRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Offline Access Available",
                                                    color = ErrorRed,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Last account: $cachedEmail",
                                                    color = ErrorRed.copy(alpha = 0.85f),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = ErrorRed.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = ErrorRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                text = "No cached account found. Connect to internet to log in.",
                                                color = ErrorRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
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
                                    modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
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

                        // EMAIL FIELD (hidden when offline)
                        AnimatedVisibility(
                            visible = isOnline,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                NoteHubTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        errorMessage = null
                                    },
                                    label = "Email Address",
                                    placeholder = "you@example.com",
                                    leadingIcon = Icons.Filled.Email,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // PASSWORD FIELD (hidden when offline)
                        AnimatedVisibility(
                            visible = isOnline,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
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
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // SIGN IN BUTTON
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    when (val result = AuthService.login(email, password)) {
                                        is AuthResult.Success -> onLoginSuccess()
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
                                containerColor = if (isOnline) PrimaryBlue else Color(0xFFF59E0B),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isOnline) Icons.Filled.Login else Icons.Filled.CloudOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isOnline) "Sign In" else "Continue Offline",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // FOOTER
                AnimatedVisibility(
                    visible = isOnline,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New to NoteHub? ",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Create Account",
                            fontSize = 15.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NoteHubTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToSignUp = {})
    }
}
