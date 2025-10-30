package com.demoapp.feature_auth.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.net.Uri
import com.demoapp.feature_auth.presentation.viewmodels.AuthViewModel
import com.demoapp.feature_auth.utils.BackendUploadService
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onProfilePhotoClick: (String) -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    // Basic Information
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Additional Information
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    // Gender options
    val genderOptions = listOf("Male", "Female")
    var showGenderDropdown by remember { mutableStateOf(false) }
    
    // Validation states
    var showValidationErrors by remember { mutableStateOf(false) }
    
    // Context and coroutine scope
    val context = LocalContext.current
    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }
    val backendUploadService = remember { BackendUploadService(context) }
    // Temporary override token for backend upload testing
    val testBearerTokenOverride: String? = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzYxODQ2MzA0LCJpYXQiOjE3NjE4NDI3MDQsImp0aSI6IjMyZTE4NmYwZGIwODRiZWY4NWU1N2E5MmUxOGI1YTk5IiwidXNlcl9pZCI6M30.aeXPjOBZMnVIKZX261taGmWIa7j6zTqvnbfjKyaptJg"
    
    // Upload states
    var isUploading by remember { mutableStateOf(false) }
    
    // Auth ViewModel state
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle registration success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a Snackbar here if needed
            viewModel.clearError()
        }
    }
    
    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            uploadError = null
            isUploading = true
            
            // Upload via backend only if authenticated
            coroutineScope.launch {
                try {
                    val token = com.demoapp.feature_auth.data.AuthTokenManager.getToken(context)
                    if (token.isNullOrBlank()) {
                        uploadError = "Please login before uploading ID document"
                        isUploading = false
                        return@launch
                    }
                    val result = backendUploadService.uploadIdImage(uri, bearerTokenOverride = testBearerTokenOverride)
                    result.fold(
                        onSuccess = { imageUrl ->
                            profileImageUrl = imageUrl
                            isUploading = false
                            android.util.Log.d("RegisterScreen", "Profile image uploaded successfully: $imageUrl")
                        },
                        onFailure = { exception ->
                            uploadError = "Failed to upload image: ${exception.message}"
                            isUploading = false
                            android.util.Log.e("RegisterScreen", "Profile image upload failed", exception)
                        }
                    )
                } catch (e: Exception) {
                    uploadError = "Upload error: ${e.message}"
                    isUploading = false
                    android.util.Log.e("RegisterScreen", "Profile image upload exception", e)
                }
            }
        } else {
            uploadError = "Failed to get image URI"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Icon Section
            Card(
                modifier = Modifier
                    .padding(bottom = 24.dp, top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "H",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // App Name
                    Text(
                        text = "HustleHub",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    Text(
                        text = "Your Work, Your Way",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Welcome Text
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Sign up to get started with your account",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            
            
            // Basic Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // First Name Field
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name *") },
                        placeholder = { Text("John") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "First Name",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && firstName.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Last Name Field
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name *") },
                        placeholder = { Text("Doe") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Last Name",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && lastName.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address *") },
                        placeholder = { Text("john.doe@example.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && (email.isEmpty() || !email.contains("@")),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Phone Number Field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("+255 123 456 789") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && phoneNumber.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        placeholder = { Text("Create a strong password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Text(
                                    text = if (passwordVisible) "🙈" else "👁",
                                    fontSize = 18.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && (password.isEmpty() || password.length < 6),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password *") },
                        placeholder = { Text("Confirm your password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Confirm Password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showValidationErrors && (confirmPassword.isEmpty() || password != confirmPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Additional Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Age Field
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        placeholder = { Text("25") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Age",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Gender Dropdown with ExposedDropdownMenu
                    ExposedDropdownMenuBox(
                        expanded = showGenderDropdown,
                        onExpandedChange = { showGenderDropdown = !showGenderDropdown }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Gender") },
                            placeholder = { Text("Select gender") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Gender",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = showGenderDropdown
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showGenderDropdown,
                            onDismissRequest = { showGenderDropdown = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        showGenderDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Address Field
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        placeholder = { Text("123 Main Street, City, Country") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Comments Field
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Comments") },
                        placeholder = { Text("Tell us about yourself...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Profile Photo Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Profile Photo Display
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { 
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isUploading -> {
                                        // Show loading indicator
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(30.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    profileImageUri != null -> {
                                        // Show selected image
                                        AsyncImage(
                                            model = profileImageUri,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    else -> {
                                        // Show add photo icon
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Photo",
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            // Photo info text
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Profile Photo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                Text(
                                    text = when {
                                        isUploading -> "Uploading..."
                                        profileImageUrl != null -> "Photo uploaded successfully"
                                        profileImageUri != null -> "Photo selected"
                                        uploadError != null -> "Upload failed: $uploadError"
                                        else -> "Tap to add photo"
                                    },
                                    fontSize = 12.sp,
                                    color = when {
                                        isUploading -> MaterialTheme.colorScheme.primary
                                        profileImageUrl != null -> Color(0xFF4CAF50) // Green for success
                                        uploadError != null -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                )
                            }
                            
                            // Edit icon
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Photo",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Create Account Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                Surface(
                    onClick = {
                        // Validate required fields
                        val isValid = firstName.isNotEmpty() && 
                                    lastName.isNotEmpty() && 
                                    email.isNotEmpty() && 
                                    email.contains("@") &&
                                    phoneNumber.isNotEmpty() && 
                                    password.isNotEmpty() && 
                                    password.length >= 6 &&
                                    password == confirmPassword
                        
                        if (isValid && !uiState.isLoading) {
                            // Call the ViewModel to register
                            viewModel.register(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phoneNumber = phoneNumber,
                                password = password,
                                passwordConfirm = confirmPassword,
                                age = age.toIntOrNull(),
                                gender = gender.takeIf { it.isNotEmpty() },
                                address = address.takeIf { it.isNotEmpty() },
                                comments = comments.takeIf { it.isNotEmpty() },
                                profilePhoto = profileImageUrl
                            )
                        } else {
                            showValidationErrors = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (uiState.isLoading) "Creating Account..." else "Create Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            
            
            // Error Display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Validation Error Message
            if (showValidationErrors) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please fill in all required fields correctly",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            
            // Login Link
            TextButton(
                onClick = onLoginClick,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Already have an account? ",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Sign In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
