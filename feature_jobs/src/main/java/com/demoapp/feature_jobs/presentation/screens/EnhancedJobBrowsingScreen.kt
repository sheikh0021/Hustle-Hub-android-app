package com.demoapp.feature_jobs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.demoapp.feature_jobs.data.WorkflowManager
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.WorkflowStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJobBrowsingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedJobType by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Newest") }
    var showFilters by remember { mutableStateOf(false) }
    
    val workflowManager = WorkflowManager.getInstance()
    
    // Sample jobs for demonstration
    val sampleJobs = remember {
        listOf(
            JobData(
                id = "job_1",
                title = "Grocery Shopping at Nakumatt",
                description = "Need someone to buy groceries from Nakumatt supermarket. List will be provided.",
                pay = 500.0,
                distance = 2.5,
                deadline = "2 hours",
                jobType = "Shopping",
                location = "Nakumatt, Westlands",
                workflowStep = WorkflowStep.REQUEST_POSTED,
                clientId = "client_1"
            ),
            JobData(
                id = "job_2",
                title = "Package Delivery to Karen",
                description = "Deliver a small package from CBD to Karen area. Package is ready for pickup.",
                pay = 800.0,
                distance = 15.0,
                deadline = "3 hours",
                jobType = "Delivery",
                location = "CBD to Karen",
                workflowStep = WorkflowStep.REQUEST_POSTED,
                clientId = "client_2"
            ),
            JobData(
                id = "job_3",
                title = "Market Survey in Eastleigh",
                description = "Conduct a market survey about mobile phone usage in Eastleigh area.",
                pay = 1200.0,
                distance = 8.0,
                deadline = "1 day",
                jobType = "Survey",
                location = "Eastleigh, Nairobi",
                workflowStep = WorkflowStep.REQUEST_POSTED,
                clientId = "client_3"
            ),
            JobData(
                id = "job_4",
                title = "Office Supplies Shopping",
                description = "Buy office supplies from Stationery World. Detailed list provided.",
                pay = 300.0,
                distance = 1.5,
                deadline = "4 hours",
                jobType = "Shopping",
                location = "Stationery World, CBD",
                workflowStep = WorkflowStep.REQUEST_POSTED,
                clientId = "client_4"
            ),
            JobData(
                id = "job_5",
                title = "Document Delivery",
                description = "Deliver important documents from Westlands to Thika. Urgent delivery required.",
                pay = 1000.0,
                distance = 25.0,
                deadline = "2 hours",
                jobType = "Delivery",
                location = "Westlands to Thika",
                workflowStep = WorkflowStep.REQUEST_POSTED,
                clientId = "client_5"
            )
        )
    }
    
    val filteredJobs = remember(sampleJobs, selectedJobType, searchQuery) {
        sampleJobs.filter { job ->
            val matchesType = selectedJobType == "All" || job.jobType == selectedJobType
            val matchesSearch = searchQuery.isBlank() || 
                               job.title.contains(searchQuery, ignoreCase = true) ||
                               job.description.contains(searchQuery, ignoreCase = true) ||
                               job.location.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Jobs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = { showFilters = !showFilters }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Filters"
                )
            }
        }
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search jobs...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Job Type Filter
        if (showFilters) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Job Type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Shopping", "Delivery", "Survey").forEach { type ->
                            FilterChip(
                                selected = selectedJobType == type,
                                onClick = { selectedJobType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Sort By",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Newest", "Highest Pay", "Deadline").forEach { sort ->
                            FilterChip(
                                selected = sortBy == sort,
                                onClick = { sortBy = sort },
                                label = { Text(sort) }
                            )
                        }
                    }
                }
            }
        }
        
        // Jobs List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredJobs) { job: JobData ->
                JobCard(
                    job = job,
                    onApplyClick = {
                        navController.navigate("contractor_application/${job.id}/${job.title}")
                    },
                    onViewDetails = {
                        navController.navigate("job_details/${job.title}")
                    }
                )
            }
        }
    }
}

@Composable
private fun JobCard(
    job: JobData,
    onApplyClick: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Job Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = job.jobType,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                Text(
                    text = "KSh ${job.pay.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Job Description
            Text(
                text = job.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Job Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.location,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.deadline,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Details")
                }
                
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply Now")
                }
            }
        }
    }
}
