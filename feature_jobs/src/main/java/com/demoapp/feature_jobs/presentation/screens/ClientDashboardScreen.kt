package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.demoapp.core.ui.TranslationManager
import com.demoapp.feature_jobs.data.NotificationRepository
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.domain.models.TaskData
import com.demoapp.feature_jobs.domain.models.TaskStatus
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.core.ui.LanguageManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.demoapp.feature_auth.presentation.viewmodels.AuthViewModel
import com.demoapp.feature_auth.data.AuthTokenManager

@Composable
fun ClientDashboardScreen(
    navController: NavController,
    clientId: String = "client_mary_johnson", // Default to Mary Johnson for demo
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        TranslationManager.getString(context, "job_request"),
        TranslationManager.getString(context, "worker")
    )
    
    // Language toggle state
    var currentLanguage by remember { 
        mutableStateOf(LanguageManager.getCurrentLanguage(context))
    }
    
    // Handle logout - only navigate if token was set and then cleared
    val authState by authViewModel.uiState.collectAsState()
    var previousToken by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(authState.isSuccess, authState.token) {
        android.util.Log.d("ClientDashboardScreen", "Auth state changed - isSuccess: ${authState.isSuccess}, token: ${if (authState.token != null) "Present" else "NULL"}, previousToken: ${if (previousToken != null) "Present" else "NULL"}")
        
        // Only navigate to auth if we had a token before and now it's null (actual logout)
        if (previousToken != null && authState.token == null && !authState.isSuccess) {
            android.util.Log.d("ClientDashboardScreen", "Logout detected - navigating to auth screen")
            // User has been logged out, navigate to login screen
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
        // Update previous token
        if (authState.token != null) {
            previousToken = authState.token
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with Wallet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TranslationManager.getString(context, "app_name"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Language Toggle Button and Logout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        val newLanguage = if (currentLanguage == "en") "sw" else "en"
                        LanguageManager.saveLanguage(context, newLanguage)
                        currentLanguage = newLanguage
                        // Trigger activity recreation to apply language change
                        if (context is com.demoapp.core.ui.LanguageAwareActivity) {
                            context.recreate()
                        }
                    }
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "🇺🇸 EN"
                            "sw" -> "🇹🇿 SW"
                            else -> "🇺🇸 EN"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Logout Button
                IconButton(
                    onClick = {
                        android.util.Log.d("ClientDashboardScreen", "Logout button clicked")
                        // Get token from SharedPreferences
                        val token = AuthTokenManager.getToken(context)
                        if (token != null) {
                            // Call logout API with token
                            authViewModel.logout(token)
                            // Clear token from SharedPreferences
                            AuthTokenManager.clearToken(context)
                        } else {
                            android.util.Log.d("ClientDashboardScreen", "No token found, clearing state")
                            authViewModel.resetState()
                        }
                        // Navigate to auth screen immediately after logout
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTabIndex = index }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        
        // Tab Content
        when (selectedTabIndex) {
            0 -> ClientTab(navController)
            1 -> WorkerTab(navController)
        }
    }
}

@Composable
private fun ClientTab(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Main title
        Text(
            text = TranslationManager.getString(context, "job_request_dashboard"),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back! 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Ready to post a new job? Use our AI assistant to create the perfect job posting in minutes.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Notification Bell
                    NotificationBell(
                        userId = "client_mary_johnson",
                        navController = navController
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Popular Job Types
        Text(
            text = "Popular Job Types",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Job Type Cards
        JobTypeCard(
            icon = "🛒",
            title = "Shopping Assistance",
            description = "Get help with grocery shopping, errands, and purchases",
            onClick = { navController.navigate("job_request_flow/SHOPPING") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        JobTypeCard(
            icon = "🚚",
            title = "Package Delivery",
            description = "Fast and reliable delivery services for your packages",
            onClick = { navController.navigate("job_request_flow/DELIVERY") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        JobTypeCard(
            icon = "📋",
            title = "Survey & Research",
            description = "Data collection, market research, and customer feedback",
            onClick = { navController.navigate("job_request_flow/SURVEY") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // My Posted Jobs Section
        MyPostedJobsSection(navController = navController, clientId = "client_mary_johnson")
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun JobTypeCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun WorkerTab(
    navController: NavController
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Available Jobs", "My Jobs")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Notification Bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Worker Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Worker Notification Bell
            WorkerNotificationBell(
                workerId = "worker_1", // Default worker ID for demo
                navController = navController
            )
        }
        
        
        // My Tasks Button - Compact Design
        Button(
            onClick = { navController.navigate("my_tasks") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋",
                    fontSize = 16.sp
                )
                Text(
                    text = "My Tasks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTabIndex = index }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar (only for Available Jobs tab)
        if (selectedTabIndex == 0) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("Search for jobs...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Tab Content
        when (selectedTabIndex) {
            0 -> AvailableJobsTab(navController, searchQuery)
            1 -> MyJobsTab(navController)
        }
    }
}

@Composable
private fun AvailableJobsTab(navController: NavController, searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Available Jobs List
        Text(
            text = "Available Jobs",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Get available jobs from repository
        val repository = JobRepositorySingleton.instance
        val jobs by repository.jobs.collectAsState()
        val availableJobs = jobs.filter { it.status == JobStatus.ACTIVE }
        
        // Filter jobs based on search query
        val filteredJobs = if (searchQuery.isEmpty()) {
            availableJobs
        } else {
            availableJobs.filter { job ->
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.description.contains(searchQuery, ignoreCase = true) ||
                job.jobType.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Display filtered jobs
        if (filteredJobs.isEmpty()) {
            Text(
                text = if (searchQuery.isEmpty()) "No available jobs at the moment" else "No jobs found matching '$searchQuery'",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                filteredJobs.forEach { job ->
                    WorkerJobCard(
                        job = job,
                        isApplied = job.workerAccepted,
                        onApply = { 
                            // Navigate to contractor application screen
                            navController.navigate("contractor_application/${job.id}/${job.title}")
                        },
                        onViewDetails = {
                            // Show job details dialog with Accept Job and Chat options
                            // For now, navigate to job details - we'll enhance this later
                            navController.navigate("job_details/${job.title}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyJobsTab(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "My Jobs",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Get accepted jobs from repository
        val repository = JobRepositorySingleton.instance
        val jobs by repository.jobs.collectAsState()
        val myJobs = jobs.filter { it.workerAccepted && (it.status == JobStatus.APPLIED || it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.COMPLETED) }
        
        // Display my jobs
        if (myJobs.isEmpty()) {
            Text(
                text = "No jobs accepted yet. Check the Available Jobs tab to find work!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                myJobs.forEach { job ->
                    JobCard(
                        title = job.title,
                        description = job.description,
                        pay = job.pay,
                        distance = job.distance,
                        deadline = job.deadline,
                        jobType = job.jobType,
                        isActive = job.status == JobStatus.IN_PROGRESS,
                        navController = navController,
                        jobData = job
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerJobCard(
    job: JobData,
    isApplied: Boolean,
    onApply: () -> Unit,
    onViewDetails: () -> Unit
) {
    var showJobDetailsDialog by remember { mutableStateOf(false) }
    var localAppliedState by remember(job.id) { mutableStateOf(isApplied) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val taskRepository = remember { TaskRepository.getInstance(context) }
    
    // Update local state when prop changes
    LaunchedEffect(isApplied) {
        localAppliedState = isApplied
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = job.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Job Type",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = job.jobType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = job.deadline,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.0f", job.pay)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${job.distance} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // View Details Button (Dark Green)
                Button(
                    onClick = { showJobDetailsDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32) // Dark green
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
                
                // Apply Now Button (Light Green)
                Button(
                    onClick = { 
                        localAppliedState = true
                        coroutineScope.launch {
                            val result = taskRepository.applyToTask(job.id)
                            result.onFailure {
                                android.util.Log.e("WorkerJobCard", "Apply failed: ${it.message}")
                                localAppliedState = false
                            }
                        }
                        onApply()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    enabled = !localAppliedState,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (localAppliedState) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            androidx.compose.ui.graphics.Color(0xFF4CAF50) // Light green
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (localAppliedState) "Applied ✓" else "Apply Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (localAppliedState) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
    
    // Job Details Dialog
    if (showJobDetailsDialog) {
        JobDetailsDialog(
            job = job,
            isApplied = isApplied,
            onDismiss = { showJobDetailsDialog = false },
            onChatWithClient = {
                onViewDetails() // Navigate to chat
                showJobDetailsDialog = false
            }
        )
    }
}

@Composable
private fun JobCard(
    title: String,
    description: String,
    pay: Double,
    distance: Double,
    deadline: String,
    jobType: String,
    isActive: Boolean,
    navController: NavController,
    jobData: JobData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Job Type",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = jobType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = deadline,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.0f", pay)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${distance} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        navController.navigate("job_details/${title}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "View Details",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = { 
                        navController.navigate("job_chat/${title}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Chat",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun JobDetailsDialog(
    job: JobData,
    isApplied: Boolean,
    onDismiss: () -> Unit,
    onChatWithClient: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = job.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = job.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pay",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$${String.format("%.0f", job.pay)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Distance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${job.distance} km",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Deadline",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = job.deadline,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Job Type: ${job.jobType}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onChatWithClient,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Chat with Job Requester")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MyPostedJobsSection(navController: NavController, clientId: String) {
    val context = LocalContext.current
    val jobRepository = JobRepositorySingleton.instance
    val jobsFlow = jobRepository.jobs.collectAsState(initial = emptyList())
    val postedJobs = jobsFlow.value.filter { 
        it.clientId == clientId && (
            it.status == JobStatus.ACTIVE || it.status == JobStatus.APPLIED || 
            it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.COMPLETED 
        )
    }
    
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = TranslationManager.getString(context, "my_posted_jobs"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${postedJobs.size} active jobs",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { 
                            navController.navigate("my_posted_jobs")
                        } 
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 12.sp
                        )
                    }
                    
                    TextButton(
                        onClick = { 
                            navController.navigate("job_poster_dashboard")
                        } 
                    ) {
                        Text(
                            text = "Manage",
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (postedJobs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "No posted jobs yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Create your first job to get started",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(postedJobs.take(3)) { job ->
                        PostedJobCard(job = job, navController = navController)
                    }
                }
                
                if (postedJobs.size > 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { navController.navigate("my_posted_jobs") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View ${postedJobs.size - 3} more jobs...")
                    }
                }
            }
        }
    }
}

@Composable
fun PostedJobCard(job: JobData, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = job.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.0f", job.pay)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Surface(
                        color = when (job.status) {
                            JobStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                            JobStatus.APPLIED -> MaterialTheme.colorScheme.secondaryContainer
                            JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                            JobStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when (job.status) {
                                JobStatus.ACTIVE -> "Active"
                                JobStatus.APPLIED -> "Applied"
                                JobStatus.IN_PROGRESS -> "In Progress"
                                JobStatus.COMPLETED -> "Completed"
                                else -> "Unknown"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Type: ${job.jobType}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                
                // View Applicants Button (for active jobs)
                if (job.status == JobStatus.ACTIVE) {
                    TextButton(
                        onClick = { navController.navigate("job_applicants/${job.id}") },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "👥 View",
                            fontSize = 9.sp,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationBell(
    userId: String,
    navController: NavController
) {
    val notificationRepository = NotificationRepository.getInstance()
    val notifications by notificationRepository.notifications.collectAsState()
    
    val unreadCount = notificationRepository.getUnreadCount(userId)
    
    BadgedBox(
        badge = {
            if (unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 10.sp
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = { navController.navigate("notifications") }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerNotificationBell(
    workerId: String,
    navController: NavController
) {
    val notificationRepository = NotificationRepository.getInstance()
    val notifications by notificationRepository.notifications.collectAsState()

    val unreadCount = notificationRepository.getUnreadCount(workerId)
    
    // Debug logging
    LaunchedEffect(notifications, workerId) {
        println("DEBUG: WorkerNotificationBell - workerId: $workerId, total notifications: ${notifications.size}, unread count: $unreadCount")
        notifications.filter { it.recipientId == workerId }.forEach { notification ->
            println("DEBUG: Worker notification - ${notification.title}: ${notification.message}")
        }
    }

    BadgedBox(
        badge = {
            if (unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 10.sp
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = { navController.navigate("worker_notifications") }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}