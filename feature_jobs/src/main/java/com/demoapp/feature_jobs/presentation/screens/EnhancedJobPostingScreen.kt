package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.data.JobCreationResult
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.LandmarkType
import com.demoapp.feature_jobs.presentation.models.JobStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJobPostingScreen(
    navController: NavController,
    repository: OfflineJobRepository
) {
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var pay by remember { mutableStateOf("") }
    var substitutes by remember { mutableStateOf("") }
    var deliveryTimeFrame by remember { mutableStateOf("") }
    var maxWeightLimit by remember { mutableStateOf("") }
    var nearbyLandmark by remember { mutableStateOf("") }
    var selectedLandmarkType by remember { mutableStateOf<LandmarkType?>(null) }
    var showWeightWarning by remember { mutableStateOf(false) }
    var showPaymentGuidance by remember { mutableStateOf(false) }
    var isDraftMode by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf<List<String>>(emptyList()) }

    val networkStatus by repository.networkStatus.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with network status
        TopAppBar(
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Post a New Job",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Network status indicator
                    Icon(
                        imageVector = if (networkStatus) Icons.Default.Home else Icons.Default.Home,
                        contentDescription = if (networkStatus) "Online" else "Offline",
                        tint = if (networkStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                // Draft mode toggle
                IconButton(
                    onClick = { 
                        isDraftMode = !isDraftMode
                    }
                ) {
                    Icon(
                        imageVector = if (isDraftMode) Icons.Default.Add else Icons.Default.Settings,
                        contentDescription = if (isDraftMode) "Save as Draft" else "Save",
                        tint = if (isDraftMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Network status banner
        if (!networkStatus) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Offline",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You're offline. Your job will be saved as a draft and synced when connection is restored.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Validation errors
            if (showValidationErrors && validationErrors.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Please fix the following errors:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            validationErrors.forEach { error ->
                                Text(
                                    text = "• $error",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Job Title
            item {
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = showValidationErrors && jobTitle.isBlank()
                )
            }

            // Job Description
            item {
                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = { jobDescription = it },
                    label = { Text("Job Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    isError = showValidationErrors && jobDescription.isBlank()
                )
            }

            // Location with landmark enhancement
            item {
                Column {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = showValidationErrors && location.isBlank()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Nearby Landmark (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Landmark type selection
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(LandmarkType.values()) { landmarkType ->
                            FilterChip(
                                onClick = { 
                                    selectedLandmarkType = if (selectedLandmarkType == landmarkType) null else landmarkType
                                },
                                label = { Text(landmarkType.name.lowercase().replace("_", " ")) },
                                selected = selectedLandmarkType == landmarkType
                            )
                        }
                    }
                    
                    if (selectedLandmarkType != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nearbyLandmark,
                            onValueChange = { nearbyLandmark = it },
                            label = { Text("Landmark Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Brand (Required)
            item {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = showValidationErrors && brand.isBlank()
                )
            }

            // Quantity and Price row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        isError = showValidationErrors && (quantity.isBlank() || quantity.toIntOrNull()?.let { it <= 0 } == true)
                    )
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        isError = showValidationErrors && (price.isBlank() || price.toDoubleOrNull()?.let { it <= 0 } == true)
                    )
                }
            }

            // Pay amount with minimum limit warning
            item {
                Column {
                    OutlinedTextField(
                        value = pay,
                        onValueChange = { 
                            pay = it
                            // Check minimum pay limit
                            val payAmount = it.toDoubleOrNull() ?: 0.0
                            if (payAmount > 0 && payAmount < OfflineJobRepository.MINIMUM_PAY_LIMIT) {
                                showWeightWarning = true
                            } else {
                                showWeightWarning = false
                            }
                        },
                        label = { Text("Pay Amount *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        isError = showValidationErrors && (pay.isBlank() || pay.toDoubleOrNull()?.let { it <= 0 } == true)
                    )
                    
                    if (showWeightWarning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Minimum pay is ${OfflineJobRepository.MINIMUM_PAY_LIMIT}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Weight limit with warning
            item {
                Column {
                    OutlinedTextField(
                        value = maxWeightLimit,
                        onValueChange = { 
                            maxWeightLimit = it
                            // Check weight limit
                            val weight = it.toDoubleOrNull() ?: 0.0
                            if (weight > OfflineJobRepository.MAX_WEIGHT_LIMIT) {
                                showWeightWarning = true
                            } else {
                                showWeightWarning = false
                            }
                        },
                        label = { Text("Maximum Weight Limit (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    if (showWeightWarning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Weight limit exceeds recommended ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Substitutes (Required)
            item {
                OutlinedTextField(
                    value = substitutes,
                    onValueChange = { substitutes = it },
                    label = { Text("Substitutes *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    isError = showValidationErrors && substitutes.isBlank()
                )
            }

            // Delivery Time Frame (Required)
            item {
                OutlinedTextField(
                    value = deliveryTimeFrame,
                    onValueChange = { deliveryTimeFrame = it },
                    label = { Text("Delivery Time Frame *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = showValidationErrors && deliveryTimeFrame.isBlank()
                )
            }

            // Penalty information
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Penalty Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Late completion: ${OfflineJobRepository.LATE_PENALTY_PERCENTAGE}% penalty",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• Job cancellation: ${OfflineJobRepository.CANCELLATION_PENALTY_PERCENTAGE}% penalty",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• Immediate cancellation after acceptance: 50% higher penalty",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Payment guidance button
                    OutlinedButton(
                        onClick = { showPaymentGuidance = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Help",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Payment Help")
                    }
                    
                    // Submit button
                    Button(
                        onClick = {
                            val newJob = JobData(
                                title = jobTitle,
                                description = jobDescription,
                                pay = pay.toDoubleOrNull() ?: 0.0,
                                distance = 0.0, // Will be calculated
                                deadline = deliveryTimeFrame,
                                jobType = "General",
                                location = location,
                                brand = brand,
                                quantity = quantity.toIntOrNull() ?: 1,
                                price = price.toDoubleOrNull() ?: 0.0,
                                substitutes = substitutes,
                                deliveryTimeFrame = deliveryTimeFrame,
                                maxWeightLimit = maxWeightLimit.toDoubleOrNull(),
                                nearbyLandmark = nearbyLandmark.takeIf { it.isNotBlank() },
                                landmarkType = selectedLandmarkType,
                                latePenalty = (pay.toDoubleOrNull() ?: 0.0) * (OfflineJobRepository.LATE_PENALTY_PERCENTAGE / 100.0),
                                latePenaltyDescription = "${OfflineJobRepository.LATE_PENALTY_PERCENTAGE}% penalty for late completion"
                            )

                            val result = if (isDraftMode || !networkStatus) {
                                repository.saveAsDraft(newJob)
                            } else {
                                repository.addJob(newJob)
                            }

                            when (result) {
                                is JobCreationResult.Success -> {
                                    navController.popBackStack()
                                }
                                is JobCreationResult.Failure -> {
                                    validationErrors = result.errors
                                    showValidationErrors = true
                                }
                            }
                        },
                        modifier = Modifier.weight(2f),
                        enabled = jobTitle.isNotBlank() && jobDescription.isNotBlank() && 
                                location.isNotBlank() && brand.isNotBlank() && 
                                quantity.isNotBlank() && price.isNotBlank() && 
                                pay.isNotBlank() && substitutes.isNotBlank() && 
                                deliveryTimeFrame.isNotBlank()
                    ) {
                        Text(
                            text = if (isDraftMode || !networkStatus) "Save as Draft" else "Post Job",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Payment guidance dialog
    if (showPaymentGuidance) {
        PaymentGuidanceDialog(
            onDismiss = { showPaymentGuidance = false },
            onProceedToPayment = {
                showPaymentGuidance = false
                // Navigate to payment screen
                navController.navigate("payment_guidance")
            }
        )
    }
}

@Composable
fun PaymentGuidanceDialog(
    onDismiss: () -> Unit,
    onProceedToPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Payment Guidance",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Our chatbot assistant will guide you through the payment process step by step.",
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Chatbot",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "AI Assistant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "Step-by-step payment guidance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceedToPayment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Start Payment Guidance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
