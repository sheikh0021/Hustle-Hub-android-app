package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.demoapp.core_network.models.ShoppingItem
import com.demoapp.feature_jobs.data.WorkflowManager
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.NetworkConnectivityManager
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.WorkflowStep
import com.demoapp.feature_jobs.presentation.viewmodels.EnhancedJobPostingViewModel
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJobPostingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: EnhancedJobPostingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EnhancedJobPostingViewModel(
                    repository = OfflineJobRepository(context, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)),
                    networkManager = NetworkConnectivityManager(context),
                    taskRepository = TaskRepository.getInstance(context)
                ) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var jobAmount by remember { mutableStateOf("") }
    var jobDeadline by remember { mutableStateOf("") }
    var proofRequired by remember { mutableStateOf(true) }
    var proofType by remember { mutableStateOf("Photos and Messages") }
    var urgencyLevel by remember { mutableStateOf("Normal") }
    var specialInstructions by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var storeServiceLocation by remember { mutableStateOf("") }
    var deliveryLocation by remember { mutableStateOf("") }
    var storeServiceLatitude by remember { mutableStateOf("") }
    var storeServiceLongitude by remember { mutableStateOf("") }
    var deliveryLatitude by remember { mutableStateOf("") }
    var deliveryLongitude by remember { mutableStateOf("") }
    
    // Shopping items
    var shoppingItems by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemBrand by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val workflowManager = WorkflowManager.getInstance()
    
    // Handle success
    LaunchedEffect(uiState.isJobCreated) {
        if (uiState.isJobCreated) {
            showSuccessDialog = true
        }
    }
    
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Error will be shown via UI state
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
                IconButton(
                onClick = { navController.popBackStack() }
                ) {
                    Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Create Job Request",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Job Type Selection Card
            Card(
            modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Job Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Shopping Job Type
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (jobType == "Shopping") 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { jobType = "Shopping" }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (jobType == "Shopping") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                    Text(
                                text = "Shopping",
                                fontSize = 12.sp,
                                color = if (jobType == "Shopping") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Delivery Job Type
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (jobType == "Delivery") 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { jobType = "Delivery" }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (jobType == "Delivery") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Delivery",
                                fontSize = 12.sp,
                                color = if (jobType == "Delivery") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Survey Job Type
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (jobType == "Survey") 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { jobType = "Survey" }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (jobType == "Survey") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                text = "Survey",
                                fontSize = 12.sp,
                                color = if (jobType == "Survey") 
                                    MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        // Job Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Job Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

            // Job Title
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title *") },
                    placeholder = { Text("Enter a clear, descriptive title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))

            // Job Description
                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = { jobDescription = it },
                    label = { Text("Job Description *") },
                    placeholder = { Text("Describe the job in detail...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Store/Service Location
                OutlinedTextField(
                    value = storeServiceLocation,
                    onValueChange = { storeServiceLocation = it },
                    label = { Text("Store/Service Location *") },
                    placeholder = { Text("e.g., 123 Main Street, Dodoma, Tanzania") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Store Location Coordinates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = storeServiceLatitude,
                        onValueChange = { storeServiceLatitude = it },
                        label = { Text("Latitude") },
                        placeholder = { Text("-6.1630") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        value = storeServiceLongitude,
                        onValueChange = { storeServiceLongitude = it },
                        label = { Text("Longitude") },
                        placeholder = { Text("35.7516") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Delivery Location
                OutlinedTextField(
                    value = deliveryLocation,
                    onValueChange = { deliveryLocation = it },
                    label = { Text("Delivery Location *") },
                    placeholder = { Text("e.g., 456 Home Street, Dodoma, Tanzania") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Delivery Location Coordinates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryLatitude,
                        onValueChange = { deliveryLatitude = it },
                        label = { Text("Latitude") },
                        placeholder = { Text("-6.1650") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    OutlinedTextField(
                        value = deliveryLongitude,
                        onValueChange = { deliveryLongitude = it },
                        label = { Text("Longitude") },
                        placeholder = { Text("35.7520") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Shopping Items Section (only for Shopping category)
                if (jobType.lowercase() == "shopping") {
                    Text(
                        text = "Shopping Items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    shoppingItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.item_name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${item.brand} - ${item.quantity}x",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "KES ${item.price * item.quantity}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(onClick = {
                                    shoppingItems = shoppingItems.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove"
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Button(
                        onClick = { showAddItemDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Shopping Item")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount and Deadline Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Amount
                    OutlinedTextField(
                        value = jobAmount,
                        onValueChange = { jobAmount = it },
                        label = { Text("Amount (KSh) *") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    // Deadline
                    OutlinedTextField(
                        value = jobDeadline,
                        onValueChange = { jobDeadline = it },
                        label = { Text("Deadline *") },
                        placeholder = { Text("e.g., 2 hours") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
        
        // Proof Requirements Card
        Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Proof Requirements",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Proof Required Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proof of completion required",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Switch(
                        checked = proofRequired,
                        onCheckedChange = { proofRequired = it }
                    )
                }
                
                if (proofRequired) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Proof Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = proofType,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Proof Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            listOf("Photos and Messages", "Photos Only", "Messages Only", "Video Recording").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { proofType = option }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Additional Options Card
        Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                    text = "Additional Options",
                    fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Urgency Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urgency Level",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = urgencyLevel,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .width(120.dp)
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            listOf("Low", "Normal", "High", "Urgent").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { urgencyLevel = option }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Special Instructions
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Special Instructions (Optional)") },
                    placeholder = { Text("Any special requirements or notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        // Submit Button
        Button(
            onClick = {
                if (jobTitle.isNotBlank() && jobDescription.isNotBlank() && 
                    jobAmount.isNotBlank() && jobDeadline.isNotBlank() && 
                    jobType.isNotBlank() && storeServiceLocation.isNotBlank() && 
                    deliveryLocation.isNotBlank()) {
                    
                    // Parse deadline to ISO 8601 format
                    // For now, assume deadline is in hours and add to current time
                    val deadlineHours = jobDeadline.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val dueDate = ZonedDateTime.now(ZoneOffset.UTC)
                        .plusHours(deadlineHours.toLong())
                        .format(DateTimeFormatter.ISO_INSTANT)
                    
                    // Parse coordinates (default to 0.0 if empty)
                    val storeLat = storeServiceLatitude.toDoubleOrNull() ?: 0.0
                    val storeLng = storeServiceLongitude.toDoubleOrNull() ?: 0.0
                    val deliveryLat = deliveryLatitude.toDoubleOrNull() ?: 0.0
                    val deliveryLng = deliveryLongitude.toDoubleOrNull() ?: 0.0
                    
                    viewModel.createTask(
                        title = jobTitle,
                        taskDescription = jobDescription,
                        category = jobType.lowercase(),
                        storeServiceLocation = storeServiceLocation,
                        deliveryLocation = deliveryLocation,
                        budgetKes = jobAmount.toDoubleOrNull() ?: 0.0,
                        dueDate = dueDate,
                        storeServiceLatitude = storeLat,
                        storeServiceLongitude = storeLng,
                        deliveryLatitude = deliveryLat,
                        deliveryLongitude = deliveryLng,
                        shoppingItems = shoppingItems
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && jobTitle.isNotBlank() && jobDescription.isNotBlank() && 
                     jobAmount.isNotBlank() && jobDeadline.isNotBlank() && 
                     jobType.isNotBlank() && storeServiceLocation.isNotBlank() && 
                     deliveryLocation.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isLoading) "Creating Job..." else "Create Job Request",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Error Message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Cancel Button
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    
    // Shopping Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Shopping Item") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = itemBrand,
                        onValueChange = { itemBrand = it },
                        label = { Text("Brand *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = itemPrice,
                            onValueChange = { itemPrice = it },
                            label = { Text("Price (KES) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = itemQuantity,
                            onValueChange = { itemQuantity = it },
                            label = { Text("Quantity *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemName.isNotBlank() && itemBrand.isNotBlank() && 
                            itemPrice.isNotBlank() && itemQuantity.isNotBlank()) {
                            shoppingItems = shoppingItems + ShoppingItem(
                                item_name = itemName,
                                brand = itemBrand,
                                price = itemPrice.toDoubleOrNull() ?: 0.0,
                                quantity = itemQuantity.toIntOrNull() ?: 1
                            )
                            itemName = ""
                            itemBrand = ""
                            itemPrice = ""
                            itemQuantity = ""
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Success Dialog
    if (showSuccessDialog) {
    AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
        title = {
            Text(
                    text = "Job Created!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
                Text("Your job request has been created and posted. Contractors can now view and apply to your job.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}