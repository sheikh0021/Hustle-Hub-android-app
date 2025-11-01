package com.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.demoapp.feature_auth.presentation.screens.LoginScreen
import com.demoapp.feature_auth.presentation.screens.RegisterScreen
import com.demoapp.feature_auth.presentation.screens.ProfilePhotoUploadScreen
import com.demoapp.feature_onboarding.presentation.screens.OnboardingScreen
import com.demoapp.feature_jobs.presentation.screens.ClientDashboardScreen
import com.demoapp.feature_jobs.presentation.screens.WorkerDashboardScreen
import com.demoapp.feature_jobs.presentation.screens.PostJobScreen
import com.demoapp.feature_jobs.presentation.screens.EnhancedJobPostingScreen
import com.demoapp.feature_jobs.presentation.screens.EnhancedJobBrowsingScreen
import com.demoapp.feature_jobs.presentation.screens.EnhancedContractorSelectionScreen
import com.demoapp.feature_jobs.presentation.screens.JobApplicationsScreen
import com.demoapp.feature_jobs.presentation.screens.JobPosterDashboardScreen
import com.demoapp.feature_jobs.presentation.screens.SpecificTaskCreationScreen
import com.demoapp.feature_jobs.presentation.screens.WorkerJobFlowScreen
import com.demoapp.feature_jobs.presentation.screens.PaymentQRCodeScreen
import com.demoapp.feature_jobs.presentation.screens.SimpleJobRequestScreen
import com.demoapp.feature_jobs.presentation.screens.SimpleWorkerScreen
import com.demoapp.feature_jobs.presentation.screens.MyTasksScreen
import com.demoapp.feature_jobs.presentation.screens.MyPostedJobsScreen
import com.demoapp.feature_jobs.presentation.screens.JobApplicantsScreen
import com.demoapp.feature_jobs.presentation.screens.NotificationsScreen
import com.demoapp.feature_jobs.presentation.screens.ContractorApplicationScreen
import com.demoapp.feature_jobs.presentation.screens.MyApplicationsScreen
import com.demoapp.feature_jobs.presentation.screens.JobDetailsScreen
import com.demoapp.feature_jobs.presentation.screens.CreateInvoiceScreen
import com.demoapp.feature_jobs.presentation.screens.JobStartChatbotScreen
import com.demoapp.feature_jobs.presentation.screens.WorkerNotificationsScreen
import com.demoapp.feature_jobs.presentation.screens.AvailableJobsScreen
import com.demoapp.feature_jobs.presentation.screens.JobChatScreen
import com.demoapp.feature_jobs.presentation.screens.WalletScreen
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.FirebaseChatRepository
import com.demoapp.feature_jobs.data.NotificationRepository
import com.demoapp.core.ui.LanguageAwareActivity
import com.demoapp.core.ui.LanguageManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.demoapp.feature_jobs.presentation.flows.RegistrationFlowScreen
import com.demoapp.feature_jobs.presentation.flows.TaskRequestFlowScreen
import com.demoapp.feature_jobs.presentation.flows.WhatsAppPaymentFlowScreen
import com.demoapp.feature_jobs.presentation.flows.WorkerNotificationFlowScreen
import com.demoapp.feature_jobs.presentation.flows.TaskExecutionFlowScreen
import com.demoapp.feature_jobs.presentation.flows.PaymentCompletionFlowScreen
import com.demoapp.feature_jobs.presentation.flows.DeliveryFlowScreen
import com.demoapp.feature_jobs.presentation.flows.ShoppingFlowScreen
import com.demoapp.feature_jobs.presentation.flows.TestFlowScreen
import com.demoapp.feature_jobs.presentation.screens.JobValidationChatbotScreen
import com.demoapp.ui.theme.DemoAppTheme

class MainActivity : LanguageAwareActivity() {
    
    private fun isOnboardingCompleted(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getBoolean("onboarding_completed", false)
    }
    
    private fun setOnboardingCompleted() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth with anonymous sign-in
        val auth = Firebase.auth
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Initialize sample data after authentication
                        JobRepositorySingleton.instance.initializeSampleData()
                        TaskRepository.getInstance(this@MainActivity).initializeSampleData()
                        NotificationRepository.getInstance().initializeSampleNotifications()
                        
                        // Initialize Firebase chat data in a coroutine
                        GlobalScope.launch {
                            FirebaseChatRepository.getInstance().initializeSampleData()
                        }
                    } else {
                        // Fallback: initialize data anyway (for development)
                        JobRepositorySingleton.instance.initializeSampleData()
                        TaskRepository.getInstance(this@MainActivity).initializeSampleData()
                        NotificationRepository.getInstance().initializeSampleNotifications()
                        
                        GlobalScope.launch {
                            FirebaseChatRepository.getInstance().initializeSampleData()
                        }
                    }
                }
        } else {
            // User already authenticated, initialize data
            JobRepositorySingleton.instance.initializeSampleData()
            TaskRepository.getInstance(this).initializeSampleData()
            NotificationRepository.getInstance().initializeSampleNotifications()
            
            GlobalScope.launch {
                FirebaseChatRepository.getInstance().initializeSampleData()
            }
        }
        
        setContent {
            DemoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Log current route changes
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    LaunchedEffect(currentBackStackEntry) {
                        android.util.Log.d("MainActivity", "Current route changed to: ${currentBackStackEntry?.destination?.route}")
                    }
                    
                    NavHost(
                        navController = navController,
                        startDestination = "onboarding"
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onGetStartedClick = { 
                                    // Navigate to auth screen
                                    navController.navigate("auth")
                                }
                            )
                        }
                        
                        composable("auth") {
                            LoginScreen(
                                onLoginSuccess = { 
                                    android.util.Log.d("MainActivity", "onLoginSuccess callback called, navigating to client_dashboard")
                                    try {
                                        navController.navigate("client_dashboard") {
                                            // Pop all screens up to onboarding (but keep onboarding)
                                            popUpTo("onboarding") { inclusive = false }
                                            // Prevent multiple instances of dashboard
                                            launchSingleTop = true
                                        }
                                        android.util.Log.d("MainActivity", "Navigation to client_dashboard completed")
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Navigation error", e)
                                    }
                                },
                                onRegisterClick = { navController.navigate("register") }
                            )
                        }
                        
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { navController.navigate("client_dashboard") },
                                onLoginClick = { navController.navigate("auth") },
                                onProfilePhotoClick = { token -> 
                                    navController.navigate("profile_photo_upload/$token")
                                }
                            )
                        }
                        
                        composable("profile_photo_upload/{token}") { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            ProfilePhotoUploadScreen(
                                navController = navController,
                                authToken = token
                            )
                        }
                        
                        composable("client_dashboard") {
                            android.util.Log.d("MainActivity", "ClientDashboardScreen composable being created")
                            ClientDashboardScreen(
                                navController = navController
                            )
                        }
                        
                        composable("worker_dashboard") {
                            WorkerDashboardScreen(
                                navController = navController,
                                workerId = "worker_1" // Default worker ID for demo
                            )
                        }
                        
                        composable("post_job") {
                            PostJobScreen(
                                navController = navController
                            )
                        }
                        
                        composable("job_request_flow/{jobType}") { backStackEntry ->
                            val jobType = backStackEntry.arguments?.getString("jobType")
                            TaskRequestFlowScreen(
                                navController = navController,
                                jobType = jobType
                            )
                        }
                        
                        composable("job_request_flow") {
                            TaskRequestFlowScreen(
                                navController = navController,
                                jobType = null
                            )
                        }
                        
                        composable("worker_notification_flow") {
                            WorkerNotificationFlowScreen(
                                navController = navController
                            )
                        }
                        
                        composable("specific_task_creation") {
                            SpecificTaskCreationScreen(
                                navController = navController
                            )
                        }
                        
                        composable("job_validation_chatbot/{jobType}/{jobId}") { backStackEntry ->
                            val jobType = backStackEntry.arguments?.getString("jobType") ?: "Shopping"
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            val jobData = com.demoapp.feature_jobs.presentation.models.JobData(
                                id = jobId,
                                title = "Job Validation",
                                description = "Validating job requirements",
                                pay = 0.0,
                                distance = 0.0,
                                deadline = "",
                                jobType = jobType,
                                status = com.demoapp.feature_jobs.presentation.models.JobStatus.DRAFT
                            )
                            JobValidationChatbotScreen(
                                navController = navController,
                                jobType = jobType,
                                initialJobData = jobData
                            )
                        }
                        
                        composable("worker_job_flow/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: "default_job"
                            TaskExecutionFlowScreen(
                                navController = navController,
                                taskId = jobId
                            )
                        }
                        
                        composable("payment_qr/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "default_task"
                            WhatsAppPaymentFlowScreen(
                                navController = navController,
                                taskId = taskId
                            )
                        }
                        
                        // Registration Flows
                        composable("register_worker") {
                            RegistrationFlowScreen(
                                navController = navController,
                                isWorker = true
                            )
                        }
                        
                        composable("register_job_request") {
                            RegistrationFlowScreen(
                                navController = navController,
                                isWorker = false
                            )
                        }
                        
                        // Task Request Flow
                        composable("task_request_flow") {
                            TaskRequestFlowScreen(
                                navController = navController
                            )
                        }
                        
                        // Worker Notification Flow
                        composable("worker_notification_flow") {
                            WorkerNotificationFlowScreen(
                                navController = navController
                            )
                        }
                        
                        // Task Execution Flow
                        composable("task_execution_flow/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "default_task"
                            TaskExecutionFlowScreen(
                                navController = navController,
                                taskId = taskId
                            )
                        }
                        
                        // Payment Completion Flow
                        composable("payment_completion/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "default_task"
                            PaymentCompletionFlowScreen(
                                navController = navController,
                                taskId = taskId
                            )
                        }
                        
                        // Delivery Flow
                        composable("delivery_flow/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "default_task"
                            DeliveryFlowScreen(
                                navController = navController,
                                taskId = taskId
                            )
                        }
                        
                        // Shopping Flow
                        composable("shopping_flow/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "default_task"
                            ShoppingFlowScreen(
                                navController = navController,
                                taskId = taskId
                            )
                        }
                        
                        // Test Flow
                        composable("test_flows") {
                            TestFlowScreen(navController = navController)
                        }
                        
                        
                        composable("my_tasks") {
                            MyTasksScreen(
                                navController = navController,
                                workerId = "worker_1", // Default worker ID for demo
                                initialTabIndex = 0
                            )
                        }
                        
                        composable("my_tasks/{tab}") { backStackEntry ->
                            val tab = backStackEntry.arguments?.getString("tab") ?: "active"
                            val initialTabIndex = when (tab) {
                                "active" -> 0
                                "applied" -> 1
                                "completed" -> 2
                                "cancelled" -> 3
                                else -> 0
                            }
                            MyTasksScreen(
                                navController = navController,
                                workerId = "worker_1", // Default worker ID for demo
                                initialTabIndex = initialTabIndex
                            )
                        }

                        composable("my_posted_jobs") {
                            MyPostedJobsScreen(
                                navController = navController
                            )
                        }

                        composable("job_applicants/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            JobApplicantsScreen(
                                jobId = jobId,
                                navController = navController
                            )
                        }

                        composable("notifications") {
                            NotificationsScreen(
                                userId = "client_mary_johnson",
                                navController = navController
                            )
                        }
                        
                        composable("worker_notifications") {
                            WorkerNotificationsScreen(
                                workerId = "worker_1", // Default worker ID for demo
                                navController = navController
                            )
                        }

                        composable("wallet") {
                            WalletScreen(
                                navController = navController
                            )
                        }
                        
                        composable("available_jobs") {
                            AvailableJobsScreen(
                                navController = navController
                            )
                        }
                        
                        composable("contractor_application/{jobId}/{jobTitle}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
                            ContractorApplicationScreen(
                                jobId = jobId,
                                jobTitle = jobTitle,
                                navController = navController
                            )
                        }
                        
                        composable("my_applications") {
                            MyApplicationsScreen(navController = navController)
                        }
                        
                        // Enhanced Workflow Screens
                        composable("enhanced_job_posting") {
                            EnhancedJobPostingScreen(navController = navController)
                        }
                        
                        composable("enhanced_job_browsing") {
                            EnhancedJobBrowsingScreen(navController = navController)
                        }
                        
    composable("enhanced_contractor_selection/{jobId}/{jobTitle}") { backStackEntry ->
        val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
        val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
        EnhancedContractorSelectionScreen(
            jobId = jobId,
            jobTitle = jobTitle,
            navController = navController
        )
    }

    composable("job_applications/{jobId}/{jobTitle}") { backStackEntry ->
        val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
        val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
        JobApplicationsScreen(
            jobId = jobId,
            jobTitle = jobTitle,
            navController = navController
        )
    }

    composable("job_poster_dashboard") {
        JobPosterDashboardScreen(navController = navController)
    }
                        
                        // Workflow Management Screens
                        composable("workflow_progress/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            // TODO: Get job data and navigate to WorkflowProgressScreen
                        }
                        
                        composable("evidence_upload/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            // TODO: Get job data and navigate to EvidenceUploadScreen
                        }
                        
                        composable("client_confirmation/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            // TODO: Get job data and navigate to ClientConfirmationScreen
                        }
                        
                        composable("payment_processing/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            // TODO: Get job data and navigate to PaymentProcessingScreen
                        }
                        
                        composable("receipt_confirmation/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            // TODO: Get job data and navigate to ReceiptConfirmationScreen
                        }
                        
                        composable("job_details/{taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                            JobDetailsScreen(
                                taskId = taskId,
                                navController = navController
                            )
                        }
                        
                        composable("job_chat/{jobTitle}") { backStackEntry ->
                            val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
                            JobChatScreen(
                                navController = navController,
                                jobTitle = jobTitle,
                                currentUserId = "worker_john_kamau",
                                currentUserType = com.demoapp.feature_jobs.data.SenderType.WORKER,
                                currentUserName = "John Kamau"
                            )
                        }
                        
                        composable("create_invoice/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            CreateInvoiceScreen(
                                jobId = jobId,
                                navController = navController
                            )
                        }
                        
                        composable("job_start_chatbot/{jobId}") { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            JobStartChatbotScreen(
                                jobId = jobId,
                                workerId = "worker_1", // This should be the specific worker who was selected
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
