package com.demoapp.feature_jobs.presentation.flows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.demoapp.feature_jobs.domain.models.*
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import kotlinx.coroutines.launch

@Composable
fun TaskSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmitTask: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Task Submission",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("You're about to create a task with the following details:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Task will be created and posted")
                Text("• You'll be redirected to payment flow")
                Text("• Payment verification required before workers can see it")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Payment Flow:",
                    fontWeight = FontWeight.Bold
                )
                Text("1. Pay manually via WhatsApp/Mobile Money")
                Text("2. Upload QR code/screenshot proof")
                Text("3. Customer Care team verifies payment")
                Text("4. Task becomes visible to workers")
                Text("5. Worker accepts and executes task")
                Text("6. You confirm completion → Worker gets paid")
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmitTask) {
                Text("Create & Pay Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRequestFlowScreen(
    navController: NavController,
    jobType: String? = null
) {
    var taskDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { 
        mutableStateOf(
            when (jobType) {
                "SHOPPING" -> TaskCategory.SHOPPING
                "DELIVERY" -> TaskCategory.DELIVERY
                "SURVEY" -> TaskCategory.SURVEY
                "PHOTOGRAPHY" -> TaskCategory.PHOTOGRAPHY
                else -> TaskCategory.OTHER
            }
        )
    }
    var budget by remember { mutableStateOf("") }
    var storeLocation by remember { mutableStateOf("") }
    var deliveryLocation by remember { mutableStateOf("") }
    var goodsList by remember { mutableStateOf(mutableListOf<GoodsItem>()) }
    var currentGoodsName by remember { mutableStateOf("") }
    var currentGoodsBrand by remember { mutableStateOf("") }
    var currentGoodsPrice by remember { mutableStateOf("") }
    var currentGoodsQuantity by remember { mutableStateOf("") }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf(listOf<String>()) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var maxWeightLimit by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val taskRepository = remember { TaskRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // Validation functions
    fun validateMandatoryFields(): List<String> {
        val errors = mutableListOf<String>()
        
        // Check basic required fields
        if (taskDescription.isBlank()) {
            errors.add("Task description is required")
        }
        if (budget.isBlank()) {
            errors.add("Budget is required")
        }
        if (storeLocation.isBlank()) {
            errors.add("Store location is required")
        }
        if (deliveryLocation.isBlank()) {
            errors.add("Delivery location is required")
        }
        
        // Check goods list for mandatory fields
        if (goodsList.isEmpty()) {
            errors.add("At least one item is required")
        } else {
            goodsList.forEachIndexed { index, item ->
                if (item.name.isBlank()) {
                    errors.add("Item ${index + 1}: Name is required")
                }
                if (item.brand.isBlank()) {
                    errors.add("Item ${index + 1}: Brand is required")
                }
                if (item.quantity <= 0) {
                    errors.add("Item ${index + 1}: Quantity must be greater than 0")
                }
                if (item.price <= 0) {
                    errors.add("Item ${index + 1}: Price must be greater than 0")
                }
            }
        }
        
        return errors
    }

    fun isFormValid(): Boolean {
        return taskDescription.isNotBlank() && 
               budget.isNotBlank() && 
               storeLocation.isNotBlank() && 
               deliveryLocation.isNotBlank() &&
               goodsList.isNotEmpty() &&
               goodsList.all { item ->
                   item.name.isNotBlank() &&
                   item.brand.isNotBlank() &&
                   item.quantity > 0 &&
                   item.price > 0
               }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enhanced Header Section with Gradient Background
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button with improved styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            navController.navigate("client_dashboard") {
                                popUpTo("client_dashboard") { inclusive = false }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Back to Dashboard",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Main Content with improved layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Task Icon with enhanced gradient background
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedCategory) {
                                TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                                TaskCategory.DELIVERY -> Icons.Default.Home
                                TaskCategory.SURVEY -> Icons.Default.Settings
                                TaskCategory.PHOTOGRAPHY -> Icons.Default.Add
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Title and Description with better styling
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = when (selectedCategory) {
                                TaskCategory.SHOPPING -> "Shopping Assistance"
                                TaskCategory.DELIVERY -> "Package Delivery"
                                TaskCategory.SURVEY -> "Survey & Research"
                                TaskCategory.PHOTOGRAPHY -> "Photography Services"
                                else -> "Task Request"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedCategory) {
                                TaskCategory.SHOPPING -> "Get help with grocery shopping, errands, and purchases"
                                TaskCategory.DELIVERY -> "Fast and reliable delivery services for your packages"
                                TaskCategory.SURVEY -> "Data collection, market research, and customer feedback"
                                TaskCategory.PHOTOGRAPHY -> "Professional photography services for your needs"
                                else -> "Provide specific task details and requirements"
                            },
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // Task Description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Task Description",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Describe what you need done *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("e.g., I need someone to buy groceries from the supermarket") }
                )

                // Category Selection
                Text(
                    text = "Task Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (jobType != null) {
                    // Show selected category as read-only when job type is provided
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (selectedCategory) {
                                    TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                                    TaskCategory.DELIVERY -> Icons.Default.Home
                                    TaskCategory.SURVEY -> Icons.Default.Star
                                    TaskCategory.PHOTOGRAPHY -> Icons.Default.Star
                                    TaskCategory.OTHER -> Icons.Default.MoreVert
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "✓ Selected",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Show category selection dropdown when no job type is provided
                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TaskCategory.values()) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = when (category) {
                                        TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                                        TaskCategory.DELIVERY -> Icons.Default.Home
                                        TaskCategory.SURVEY -> Icons.Default.Star
                                        TaskCategory.PHOTOGRAPHY -> Icons.Default.Star
                                        TaskCategory.OTHER -> Icons.Default.MoreVert
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Task Refinement Section
        if (selectedCategory == TaskCategory.SHOPPING || selectedCategory == TaskCategory.DELIVERY || selectedCategory == TaskCategory.SURVEY) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Task Specification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Text(
                        text = when (selectedCategory) {
                            TaskCategory.SHOPPING -> "For shopping tasks, specify exact items with brand and price:"
                            TaskCategory.DELIVERY -> "For delivery tasks, specify package details and requirements:"
                            TaskCategory.SURVEY -> "For survey tasks, specify survey details and target audience:"
                            else -> "Specify task details:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    when (selectedCategory) {
                        TaskCategory.SHOPPING -> {
                            // Shopping Entry Form
                            OutlinedTextField(
                                value = currentGoodsName,
                                onValueChange = { currentGoodsName = it },
                                label = { Text("Item Name *") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentGoodsBrand,
                                    onValueChange = { currentGoodsBrand = it },
                                    label = { Text("Brand *") },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsPrice,
                                    onValueChange = { currentGoodsPrice = it },
                                    label = { Text("Price *") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsQuantity,
                                    onValueChange = { currentGoodsQuantity = it },
                                    label = { Text("Qty *") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        TaskCategory.DELIVERY -> {
                            // Delivery Entry Form
                            OutlinedTextField(
                                value = currentGoodsName,
                                onValueChange = { currentGoodsName = it },
                                label = { Text("Package Description") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentGoodsBrand,
                                    onValueChange = { currentGoodsBrand = it },
                                    label = { Text("Type") },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsPrice,
                                    onValueChange = { currentGoodsPrice = it },
                                    label = { Text("Weight") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsQuantity,
                                    onValueChange = { currentGoodsQuantity = it },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        TaskCategory.SURVEY -> {
                            // Survey Entry Form
                            OutlinedTextField(
                                value = currentGoodsName,
                                onValueChange = { currentGoodsName = it },
                                label = { Text("Survey Topic") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentGoodsBrand,
                                    onValueChange = { currentGoodsBrand = it },
                                    label = { Text("Audience") },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsPrice,
                                    onValueChange = { currentGoodsPrice = it },
                                    label = { Text("Duration") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                OutlinedTextField(
                                    value = currentGoodsQuantity,
                                    onValueChange = { currentGoodsQuantity = it },
                                    label = { Text("People") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        else -> {
                            // Default form
                            OutlinedTextField(
                                value = currentGoodsName,
                                onValueChange = { currentGoodsName = it },
                                label = { Text("Task Details") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentGoodsName.isNotBlank() && currentGoodsBrand.isNotBlank() && 
                                currentGoodsPrice.isNotBlank() && currentGoodsQuantity.isNotBlank()) {
                                goodsList.add(
                                    GoodsItem(
                                        name = currentGoodsName,
                                        brand = currentGoodsBrand,
                                        description = currentGoodsName,
                                        price = currentGoodsPrice.toDoubleOrNull() ?: 0.0,
                                        quantity = currentGoodsQuantity.toIntOrNull() ?: 1,
                                        category = "General"
                                    )
                                )
                                currentGoodsName = ""
                                currentGoodsBrand = ""
                                currentGoodsPrice = ""
                                currentGoodsQuantity = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentGoodsName.isNotBlank() && currentGoodsBrand.isNotBlank() && 
                                 currentGoodsPrice.isNotBlank() && currentGoodsQuantity.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (selectedCategory) {
                                TaskCategory.SHOPPING -> "Add Item"
                                TaskCategory.DELIVERY -> "Add Package"
                                TaskCategory.SURVEY -> "Add Survey Detail"
                                else -> "Add Item"
                            }
                        )
                    }

                    // Goods List
                    if (goodsList.isNotEmpty()) {
                        Text(
                            text = when (selectedCategory) {
                                TaskCategory.SHOPPING -> "Items to Purchase:"
                                TaskCategory.DELIVERY -> "Packages to Deliver:"
                                TaskCategory.SURVEY -> "Survey Details:"
                                else -> "Items:"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        goodsList.forEachIndexed { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
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
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Brand: ${item.brand}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "Qty: ${item.quantity} | Price: KES ${item.price}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { goodsList.removeAt(index) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Location Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Location Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = storeLocation,
                    onValueChange = { storeLocation = it },
                    label = { Text("Store/Service Location *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = "Store")
                    }
                )

                OutlinedTextField(
                    value = deliveryLocation,
                    onValueChange = { deliveryLocation = it },
                    label = { Text("Delivery Location *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Delivery")
                    }
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget (KES) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = "Budget")
                    },
                    supportingText = {
                        Text(
                            text = "Minimum: KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                )

                // Weight Limit Field (for Shopping and Delivery)
                if (selectedCategory == TaskCategory.SHOPPING || selectedCategory == TaskCategory.DELIVERY) {
                    OutlinedTextField(
                        value = maxWeightLimit,
                        onValueChange = { maxWeightLimit = it },
                        label = { Text("Maximum Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = "Weight")
                        },
                        supportingText = {
                            Text(
                                text = "Maximum recommended: ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }

        // Task Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Task Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Text(
                    text = "Category: ${selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                
                if (goodsList.isNotEmpty()) {
                    Text(
                        text = "Items: ${goodsList.size} items",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                Text(
                    text = "Budget: KES $budget",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Submit Button
        Button(
            onClick = { 
                // Validate mandatory fields before navigation
                val errors = validateMandatoryFields()
                if (errors.isEmpty()) {
                    val jobId = "job_${System.currentTimeMillis()}"
                    navController.navigate("job_validation_chatbot/${selectedCategory.name}/$jobId")
                } else {
                    validationErrors = errors
                    showValidationDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid()
        ) {
            Text(
                text = "Create Task & Pay",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Validation Error Dialog
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            title = {
                Text(
                    text = "Validation Error",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text("Please fix the following issues:")
                    Spacer(modifier = Modifier.height(8.dp))
                    validationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Task Submission Dialog
    if (showPaymentDialog) {
        TaskSubmissionDialog(
            onDismiss = { showPaymentDialog = false },
            onSubmitTask = {
                showPaymentDialog = false
                // Create task and navigate to payment flow
                val newTask = TaskData(
                    id = java.util.UUID.randomUUID().toString(),
                    title = taskDescription,
                    description = taskDescription,
                    category = selectedCategory,
                    status = TaskStatus.PENDING_PAYMENT,
                    budget = budget.toDoubleOrNull() ?: 0.0,
                    location = TaskLocation(
                        storeLocation = LocationDetails(
                            address = storeLocation,
                            latitude = 0.0,
                            longitude = 0.0,
                            city = "N/A"
                        ),
                        deliveryLocation = LocationDetails(
                            address = deliveryLocation,
                            latitude = 0.0,
                            longitude = 0.0,
                            city = "N/A"
                        )
                    ),
                    goods = goodsList.toList(),
                    paymentStatus = com.demoapp.feature_jobs.domain.models.PaymentStatus.PENDING,
                    jobRequesterId = "job_requester_1" // Default requester ID
                )
                coroutineScope.launch {
                    // TODO: Convert TaskData to API format and call createTask
                    // For now, this is a stub - the actual API call should use the proper format
                    android.util.Log.w("TaskRequestFlowScreen", "createTask call needs to be converted to API format")
                }
                // Navigate to payment flow instead of dashboard
                navController.navigate("payment_qr/${newTask.id}")
            }
        )
    }
}
