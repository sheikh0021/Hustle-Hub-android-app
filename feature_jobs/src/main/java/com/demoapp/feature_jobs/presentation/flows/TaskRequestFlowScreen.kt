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
    var isCreatingTask by remember { mutableStateOf(false) }
    
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
        // Refined Header Section - Clean and Modern Design
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Clean Top Bar with Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { 
                                navController.navigate("client_dashboard") {
                                    popUpTo("client_dashboard") { inclusive = false }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Post New Job",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category Icon and Title Section - Centered and Clean
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with subtle background
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedCategory) {
                                TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                                TaskCategory.DELIVERY -> Icons.Default.Home
                                TaskCategory.SURVEY -> Icons.Default.Star
                                TaskCategory.PHOTOGRAPHY -> Icons.Default.Add
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title - Centered
                    Text(
                        text = when (selectedCategory) {
                            TaskCategory.SHOPPING -> "Shopping Assistance"
                            TaskCategory.DELIVERY -> "Package Delivery"
                            TaskCategory.SURVEY -> "Survey & Research"
                            TaskCategory.PHOTOGRAPHY -> "Photography Services"
                            else -> "Task Request"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description - Centered with proper line height
                    Text(
                        text = when (selectedCategory) {
                            TaskCategory.SHOPPING -> "Get help with grocery shopping, errands, and purchases"
                            TaskCategory.DELIVERY -> "Fast and reliable delivery services for your packages"
                            TaskCategory.SURVEY -> "Data collection, market research, and customer feedback"
                            TaskCategory.PHOTOGRAPHY -> "Professional photography services for your needs"
                            else -> "Provide specific task details and requirements"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
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
                                // Create a new list to trigger recomposition
                                goodsList = (goodsList + GoodsItem(
                                    name = currentGoodsName,
                                    brand = currentGoodsBrand,
                                    description = currentGoodsName,
                                    price = currentGoodsPrice.toDoubleOrNull() ?: 0.0,
                                    quantity = currentGoodsQuantity.toIntOrNull() ?: 1,
                                    category = "General"
                                )).toMutableList()
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
                                        onClick = { 
                                            // Create a new list to trigger recomposition
                                            goodsList = goodsList.filterIndexed { i, _ -> i != index }.toMutableList()
                                        }
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Task Summary",
                    fontSize = 18.sp, // Increased from 16.sp
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, // Changed from onTertiaryContainer to black
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Category row with aligned text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        fontSize = 14.sp, // Increased from 12.sp
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface, // Changed to black
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp, // Increased from 12.sp
                        color = MaterialTheme.colorScheme.onSurface, // Changed to black
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // Items row with aligned text (if items exist)
                if (goodsList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items",
                            fontSize = 14.sp, // Increased from 12.sp
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface, // Changed to black
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${goodsList.size} items",
                            fontSize = 14.sp, // Increased from 12.sp
                            color = MaterialTheme.colorScheme.onSurface, // Changed to black
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                
                // Budget row with aligned text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget",
                        fontSize = 14.sp, // Increased from 12.sp
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface, // Changed to black
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "KES $budget",
                        fontSize = 14.sp, // Increased from 12.sp
                        color = MaterialTheme.colorScheme.onSurface, // Changed to black
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = { 
                // Validate mandatory fields before creating task
                val errors = validateMandatoryFields()
                if (errors.isEmpty()) {
                    // Create task via backend API first
                    isCreatingTask = true
                    coroutineScope.launch {
                        try {
                            // Build the request based on category
                            val req = when (selectedCategory) {
                                TaskCategory.SHOPPING -> {
                                    com.demoapp.core_network.models.TaskCreateRequest(
                                        title = when (selectedCategory) {
                                            TaskCategory.SHOPPING -> "Grocery Shopping"
                                            TaskCategory.DELIVERY -> "Package Delivery"
                                            TaskCategory.SURVEY -> "Survey Task"
                                            else -> taskDescription.ifBlank { "Task" }
                                        },
                                        task_description = taskDescription.ifBlank { "Task request" },
                                        category = "shopping",
                                        store_service_location = storeLocation,
                                        delivery_location = deliveryLocation,
                                        budget_kes = budget.toDoubleOrNull() ?: 0.0,
                                        due_date = java.time.Instant.now().plusSeconds(86400).toString(),
                                        store_service_latitude = null,
                                        store_service_longitude = null,
                                        delivery_latitude = null,
                                        delivery_longitude = null,
                                        shopping_items = goodsList.map {
                                            com.demoapp.core_network.models.ShoppingItemRequest(
                                                item_name = it.name,
                                                brand = it.brand.ifBlank { null },
                                                price = it.price,
                                                quantity = it.quantity
                                            )
                                        },
                                        package_items = null,
                                        survey_items = null
                                    )
                                }
                                TaskCategory.DELIVERY -> {
                                    com.demoapp.core_network.models.TaskCreateRequest(
                                        title = "Package Delivery",
                                        task_description = taskDescription.ifBlank { "Package delivery request" },
                                        category = "delivery",
                                        store_service_location = storeLocation,
                                        delivery_location = deliveryLocation,
                                        budget_kes = budget.toDoubleOrNull() ?: 0.0,
                                        due_date = java.time.Instant.now().plusSeconds(86400).toString(),
                                        store_service_latitude = null,
                                        store_service_longitude = null,
                                        delivery_latitude = null,
                                        delivery_longitude = null,
                                        shopping_items = null,
                                        package_items = goodsList.map {
                                            com.demoapp.core_network.models.PackageItemRequest(
                                                package_description = it.name,
                                                package_type = it.brand.ifBlank { "General" },
                                                weight = it.price, // Using price field for weight
                                                quantity = it.quantity
                                            )
                                        },
                                        survey_items = null
                                    )
                                }
                                TaskCategory.SURVEY -> {
                                    com.demoapp.core_network.models.TaskCreateRequest(
                                        title = "Survey Task",
                                        task_description = taskDescription.ifBlank { "Survey request" },
                                        category = "survey",
                                        store_service_location = storeLocation,
                                        delivery_location = deliveryLocation,
                                        budget_kes = budget.toDoubleOrNull() ?: 0.0,
                                        due_date = java.time.Instant.now().plusSeconds(86400).toString(),
                                        store_service_latitude = null,
                                        store_service_longitude = null,
                                        delivery_latitude = null,
                                        delivery_longitude = null,
                                        shopping_items = null,
                                        package_items = null,
                                        survey_items = goodsList.map {
                                            com.demoapp.core_network.models.SurveyItemRequest(
                                                survey_topic = it.name,
                                                audience = it.brand.ifBlank { "General" },
                                                duration = it.price.toInt(), // Using price as duration
                                                number_of_people = it.quantity
                                            )
                                        }
                                    )
                                }
                                else -> {
                                    com.demoapp.core_network.models.TaskCreateRequest(
                                        title = taskDescription.ifBlank { "Task" },
                                        task_description = taskDescription.ifBlank { "Task request" },
                                        category = "shopping",
                                        store_service_location = storeLocation,
                                        delivery_location = deliveryLocation,
                                        budget_kes = budget.toDoubleOrNull() ?: 0.0,
                                        due_date = java.time.Instant.now().plusSeconds(86400).toString(),
                                        store_service_latitude = null,
                                        store_service_longitude = null,
                                        delivery_latitude = null,
                                        delivery_longitude = null,
                                        shopping_items = goodsList.map {
                                            com.demoapp.core_network.models.ShoppingItemRequest(
                                                item_name = it.name,
                                                brand = it.brand.ifBlank { null },
                                                price = it.price,
                                                quantity = it.quantity
                                            )
                                        },
                                        package_items = null,
                                        survey_items = null
                                    )
                                }
                            }

                            android.util.Log.d("TaskRequestFlowScreen", "Creating task via API...")
                            val result = taskRepository.createTask(req)
                            result.fold(
                                onSuccess = { resp ->
                                    val created = resp.data
                                    val backendTaskId = created?.id?.toString() ?: System.currentTimeMillis().toString()
                                    android.util.Log.d("TaskRequestFlowScreen", "Task created successfully with ID: $backendTaskId")
                                    
                                    // Add to local repository for UI display
                                    val jobData = JobData(
                                        id = backendTaskId,
                                        title = created?.title ?: req.title ?: "Task",
                                        description = created?.task_description ?: req.task_description,
                                        pay = created?.budget_kes?.toDoubleOrNull() ?: req.budget_kes,
                                        distance = 0.0,
                                        deadline = created?.due_date ?: "",
                                        jobType = created?.category ?: req.category,
                                        clientId = "client_mary_johnson",
                                        status = com.demoapp.feature_jobs.presentation.models.JobStatus.ACTIVE,
                                        deliveryAddress = created?.delivery_location
                                    )
                                    com.demoapp.feature_jobs.data.JobRepositorySingleton.instance.addJob(jobData)
                                    
                                    // Navigate to payment screen with the backend task ID
                                    navController.navigate("payment_qr/$backendTaskId")
                                },
                                onFailure = { error ->
                                    android.util.Log.e("TaskRequestFlowScreen", "Create task failed: ${error.message}")
                                    isCreatingTask = false
                                    // Show error - could show a dialog here
                                    validationErrors = listOf("Failed to create task: ${error.message}")
                                    showValidationDialog = true
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("TaskRequestFlowScreen", "Exception creating task", e)
                            isCreatingTask = false
                            validationErrors = listOf("Error: ${e.message}")
                            showValidationDialog = true
                        }
                    }
                } else {
                    validationErrors = errors
                    showValidationDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid() && !isCreatingTask
        ) {
            if (isCreatingTask) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Task...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = "Create Task & Pay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                // Create task via backend and navigate to payment flow
                coroutineScope.launch {
                    try {
                        val req = com.demoapp.core_network.models.TaskCreateRequest(
                            title = when (selectedCategory) {
                                TaskCategory.SHOPPING -> "Grocery Shopping"
                                TaskCategory.DELIVERY -> "Package Delivery"
                                TaskCategory.SURVEY -> "Survey Task"
                                else -> taskDescription.ifBlank { "Task" }
                            },
                            task_description = taskDescription.ifBlank { "Task request" },
                            category = when (selectedCategory) {
                                TaskCategory.SHOPPING -> "shopping"
                                TaskCategory.DELIVERY -> "delivery"
                                TaskCategory.SURVEY -> "survey"
                                else -> "shopping"
                            },
                            store_service_location = storeLocation,
                            delivery_location = deliveryLocation,
                            budget_kes = budget.toDoubleOrNull() ?: 0.0,
                            due_date = java.time.Instant.now().plusSeconds(86400).toString(),
                            store_service_latitude = null,
                            store_service_longitude = null,
                            delivery_latitude = null,
                            delivery_longitude = null,
                            shopping_items = if (selectedCategory == TaskCategory.SHOPPING) goodsList.map {
                                com.demoapp.core_network.models.ShoppingItemRequest(
                                    item_name = it.name,
                                    brand = it.brand.ifBlank { null },
                                    price = it.price,
                                    quantity = it.quantity
                                )
                            } else null,
                            package_items = null,
                            survey_items = null
                        )

                        val result = taskRepository.createTask(req)
                        result.fold(
                            onSuccess = { resp ->
                                val created = resp.data
                                val localJobId = (created?.id ?: System.currentTimeMillis().toInt()).toString()
                                val jobData = JobData(
                                    id = localJobId,
                                    title = created?.title ?: req.title ?: "Task",
                                    description = created?.task_description ?: req.task_description,
                                    pay = created?.budget_kes?.toDoubleOrNull() ?: req.budget_kes,
                                    distance = 0.0,
                                    deadline = created?.due_date ?: "",
                                    jobType = created?.category ?: req.category,
                                    clientId = "client_mary_johnson",
                                    status = com.demoapp.feature_jobs.presentation.models.JobStatus.ACTIVE,
                                    deliveryAddress = created?.delivery_location
                                )
                                com.demoapp.feature_jobs.data.JobRepositorySingleton.instance.addJob(jobData)
                                navController.navigate("payment_qr/${jobData.id}")
                            },
                            onFailure = {
                                android.util.Log.e("TaskRequestFlowScreen", "Create task failed: ${it.message}")
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("TaskRequestFlowScreen", "Error creating task", e)
                    }
                }
            }
        )
    }
}
