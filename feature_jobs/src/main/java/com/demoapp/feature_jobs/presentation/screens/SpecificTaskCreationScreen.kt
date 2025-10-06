package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.presentation.models.JobData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificTaskCreationScreen(
    navController: NavController
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var storeLocation by remember { mutableStateOf("") }
    var deliveryLocation by remember { mutableStateOf("") }
    var goodsList by remember { mutableStateOf(mutableListOf<GoodsItem>()) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf(listOf<String>()) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var maxWeightLimit by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Create Specific Task",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ) 
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Create specific task with exact goods, brand, and price",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Task Title
            item {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Task Description
            item {
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Task Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Store Location
            item {
                OutlinedTextField(
                    value = storeLocation,
                    onValueChange = { storeLocation = it },
                    label = { Text("Store Location *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Delivery Location
            item {
                OutlinedTextField(
                    value = deliveryLocation,
                    onValueChange = { deliveryLocation = it },
                    label = { Text("Delivery Location *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Weight Limit Field
            item {
                OutlinedTextField(
                    value = maxWeightLimit,
                    onValueChange = { maxWeightLimit = it },
                    label = { Text("Maximum Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = "Maximum recommended: ${OfflineJobRepository.MAX_WEIGHT_LIMIT}kg",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            
            // Goods Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goods Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Button(
                        onClick = { 
                            goodsList.add(GoodsItem("", "", "", 1, 0.0, ""))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }
            
            // Goods List
            items(goodsList.size) { index ->
                GoodsItemCard(
                    goodsItem = goodsList[index],
                    onUpdate = { updatedItem ->
                        goodsList[index] = updatedItem
                        goodsList = goodsList.toMutableList()
                    },
                    onDelete = {
                        goodsList.removeAt(index)
                        goodsList = goodsList.toMutableList()
                    }
                )
            }
            
            // Create Task Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        if (isTaskValid(taskTitle, taskDescription, storeLocation, deliveryLocation, goodsList)) {
                            // Navigate to chatbot validation
                            val jobId = "job_${System.currentTimeMillis()}"
                            navController.navigate("job_validation_chatbot/Shopping/$jobId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isTaskValid(taskTitle, taskDescription, storeLocation, deliveryLocation, goodsList)
                ) {
                    Text(
                        text = "Create Task & Proceed to Payment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
    
    // Payment Dialog
    if (showPaymentDialog) {
        PaymentQRDialog(
            onDismiss = { showPaymentDialog = false },
            onProceedToPayment = {
                showPaymentDialog = false
                // Navigate to payment screen
                navController.navigate("payment_qr/task_${System.currentTimeMillis()}")
            }
        )
    }
}

@Composable
fun GoodsItemCard(
    goodsItem: GoodsItem,
    onUpdate: (GoodsItem) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item ${goodsItem.name.ifEmpty { "New Item" }}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Item Name
            OutlinedTextField(
                value = goodsItem.name,
                onValueChange = { onUpdate(goodsItem.copy(name = it)) },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Brand
            OutlinedTextField(
                value = goodsItem.brand,
                onValueChange = { onUpdate(goodsItem.copy(brand = it)) },
                label = { Text("Brand *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quantity
                OutlinedTextField(
                    value = goodsItem.quantity.toString(),
                    onValueChange = { 
                        val quantity = it.toIntOrNull() ?: 1
                        onUpdate(goodsItem.copy(quantity = quantity))
                    },
                    label = { Text("Quantity *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Price
                OutlinedTextField(
                    value = goodsItem.price.toString(),
                    onValueChange = { 
                        val price = it.toDoubleOrNull() ?: 0.0
                        onUpdate(goodsItem.copy(price = price))
                    },
                    label = { Text("Price *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    supportingText = {
                        Text(
                            text = "Min: KES ${OfflineJobRepository.MINIMUM_PAY_LIMIT}",
                            color = if (goodsItem.price < OfflineJobRepository.MINIMUM_PAY_LIMIT) 
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Substitutes
            OutlinedTextField(
                value = goodsItem.substitutes,
                onValueChange = { onUpdate(goodsItem.copy(substitutes = it)) },
                label = { Text("Substitutes *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun PaymentQRDialog(
    onDismiss: () -> Unit,
    onProceedToPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Payment Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "To complete your task creation, you need to make a temporary payment.",
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
                            Icons.Default.Info,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Submit QR Code of Payment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "Our CC team will confirm the payment",
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
                Text("Proceed to Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class GoodsItem(
    val name: String,
    val brand: String,
    val description: String,
    val quantity: Int,
    val price: Double,
    val substitutes: String = "" // Added substitutes field
)

fun isTaskValid(
    title: String,
    description: String,
    storeLocation: String,
    deliveryLocation: String,
    goodsList: List<GoodsItem>
): Boolean {
    return title.isNotBlank() && 
           description.isNotBlank() && 
           storeLocation.isNotBlank() && 
           deliveryLocation.isNotBlank() && 
           goodsList.isNotEmpty() &&
           goodsList.all { 
               it.name.isNotBlank() && 
               it.brand.isNotBlank() && 
               it.price > 0 && 
               it.quantity > 0 &&
               it.substitutes.isNotBlank()
           }
}
