package com.demoapp.feature_jobs.presentation.flows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingFlowScreen(
    navController: NavController,
    taskId: String
) {
    val taskRepository = remember { TaskRepository.getInstance() }
    var task by remember { mutableStateOf<TaskData?>(null) }
    var currentStep by remember { mutableStateOf(ShoppingStep.AT_STORE) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var photosTaken by remember { mutableStateOf(false) }
    var clientConfirmed by remember { mutableStateOf(false) }
    var purchaseCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        task = taskRepository.getTaskById(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Flow: ${task?.title ?: "Loading..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shopping Task Summary
            task?.let { currentTask ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Shopping Task Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Store: ${currentTask.location.storeLocation.address}")
                        Text("Delivery: ${currentTask.location.deliveryLocation.address}")
                        Text("Budget: KES ${currentTask.budget}")
                        Text("Items: ${currentTask.goods.size} items")
                    }
                }

                // Shopping Items List
                if (currentTask.goods.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Shopping List",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            currentTask.goods.forEach { item ->
                                ShoppingItemRow(item = item)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shopping Steps
                Text(
                    text = "Shopping Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Step 1: At Store
                ShoppingStepCard(
                    step = ShoppingStep.AT_STORE,
                    title = "Arrive at Store",
                    description = "Confirm you have reached the store location",
                    isCompleted = currentStep.ordinal > ShoppingStep.AT_STORE.ordinal,
                    isCurrent = currentStep == ShoppingStep.AT_STORE,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == ShoppingStep.AT_STORE) {
                            currentStep = ShoppingStep.TAKE_PHOTOS
                        }
                    }
                )

                // Step 2: Take Photos
                ShoppingStepCard(
                    step = ShoppingStep.TAKE_PHOTOS,
                    title = "Take Photos of Items",
                    description = "Take pictures of items before purchase",
                    isCompleted = currentStep.ordinal > ShoppingStep.TAKE_PHOTOS.ordinal,
                    isCurrent = currentStep == ShoppingStep.TAKE_PHOTOS,
                    icon = Icons.Default.Star,
                    onAction = {
                        if (currentStep == ShoppingStep.TAKE_PHOTOS) {
                            showPhotoDialog = true
                        }
                    }
                )

                // Step 3: Client Confirmation
                ShoppingStepCard(
                    step = ShoppingStep.CLIENT_CONFIRMATION,
                    title = "Client Confirmation",
                    description = "Wait for client to confirm items before purchase",
                    isCompleted = currentStep.ordinal > ShoppingStep.CLIENT_CONFIRMATION.ordinal,
                    isCurrent = currentStep == ShoppingStep.CLIENT_CONFIRMATION,
                    icon = Icons.Default.Email,
                    onAction = {
                        if (currentStep == ShoppingStep.CLIENT_CONFIRMATION) {
                            showConfirmationDialog = true
                        }
                    }
                )

                // Step 4: Purchase Items
                ShoppingStepCard(
                    step = ShoppingStep.PURCHASE_ITEMS,
                    title = "Purchase Items",
                    description = "Buy the confirmed items",
                    isCompleted = currentStep.ordinal > ShoppingStep.PURCHASE_ITEMS.ordinal,
                    isCurrent = currentStep == ShoppingStep.PURCHASE_ITEMS,
                    icon = Icons.Default.ShoppingCart,
                    onAction = {
                        if (currentStep == ShoppingStep.PURCHASE_ITEMS) {
                            currentStep = ShoppingStep.DELIVER_ITEMS
                            purchaseCompleted = true
                        }
                    }
                )

                // Step 5: Deliver Items
                ShoppingStepCard(
                    step = ShoppingStep.DELIVER_ITEMS,
                    title = "Deliver Items",
                    description = "Deliver purchased items to client",
                    isCompleted = currentStep.ordinal > ShoppingStep.DELIVER_ITEMS.ordinal,
                    isCurrent = currentStep == ShoppingStep.DELIVER_ITEMS,
                    icon = Icons.Default.Home,
                    onAction = {
                        if (currentStep == ShoppingStep.DELIVER_ITEMS) {
                            navController.navigate("payment_completion/$taskId")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Communication Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Communication",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { /* Open chat with client */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chat with Job Requester")
                        }
                    }
                }

                // Complete Shopping Button
                if (currentStep == ShoppingStep.DELIVER_ITEMS && purchaseCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("payment_completion/$taskId") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Complete Shopping & Process Payment", fontSize = 18.sp)
                    }
                }
            } ?: run {
                Text("Loading shopping details...", fontSize = 18.sp)
            }
        }
    }

    // Photo Dialog
    if (showPhotoDialog) {
        PhotoDialog(
            onDismiss = { showPhotoDialog = false },
            onPhotosTaken = {
                photosTaken = true
                currentStep = ShoppingStep.CLIENT_CONFIRMATION
            }
        )
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showConfirmationDialog = false },
            onConfirmed = {
                clientConfirmed = true
                currentStep = ShoppingStep.PURCHASE_ITEMS
            }
        )
    }
}

@Composable
fun ShoppingItemRow(item: com.demoapp.feature_jobs.domain.models.GoodsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.name} - ${item.brand}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Qty: ${item.quantity} | Price: KES ${item.price}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ShoppingStepCard(
    step: ShoppingStep,
    title: String,
    description: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isCurrent) onAction() },
        color = when {
            isCompleted -> MaterialTheme.colorScheme.primaryContainer
            isCurrent -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isCompleted) Color.Green else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color.Green else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isCurrent) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Confirm", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PhotoDialog(
    onDismiss: () -> Unit,
    onPhotosTaken: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Take Photos") },
        text = {
            Column {
                Text("Take clear photos of the items you are about to purchase:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. Take photos of each item")
                Text("2. Include price tags if visible")
                Text("3. Show item condition")
                Text("4. Wait for client confirmation before purchasing")
            }
        },
        confirmButton = {
            Button(onClick = onPhotosTaken) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Photos Taken")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Client Confirmation") },
        text = {
            Text("The client has confirmed the items. You can now proceed with the purchase.")
        },
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text("Proceed to Purchase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class ShoppingStep {
    AT_STORE,
    TAKE_PHOTOS,
    CLIENT_CONFIRMATION,
    PURCHASE_ITEMS,
    DELIVER_ITEMS
}
