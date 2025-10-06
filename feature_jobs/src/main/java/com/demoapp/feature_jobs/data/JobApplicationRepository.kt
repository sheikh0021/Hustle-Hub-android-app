package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.JobApplication
import com.demoapp.feature_jobs.presentation.models.ApplicationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JobApplicationRepository {
    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: JobApplicationRepository? = null

        fun getInstance(): JobApplicationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JobApplicationRepository().also { INSTANCE = it }
            }
        }
    }

    /**
     * Apply for a job
     */
    fun applyForJob(
        jobId: String,
        workerId: String,
        workerName: String,
        workerPhone: String,
        workerRating: Float = 0f,
        workerCompletedTasks: Int = 0,
        applicationMessage: String? = null,
        jobRepository: com.demoapp.feature_jobs.data.JobRepository? = null
    ): JobApplication {
        val application = JobApplication(
            id = "app_${System.currentTimeMillis()}",
            jobId = jobId,
            workerId = workerId,
            workerName = workerName,
            workerPhone = workerPhone,
            workerRating = workerRating,
            workerCompletedTasks = workerCompletedTasks,
            applicationMessage = applicationMessage
        )

        val currentApplications = _applications.value.toMutableList()
        currentApplications.add(application)
        _applications.value = currentApplications

        // Get job details and create notification for the specific job poster
        val job = jobRepository?.getJobById(jobId)
        if (job != null) {
            val notificationRepository = NotificationRepository.getInstance()
            notificationRepository.createJobApplicationNotification(
                jobId = jobId,
                jobTitle = job.title,
                clientId = job.clientId, // Use the actual client ID from the job
                workerId = workerId,
                workerName = workerName
            )
        }

        return application
    }

    /**
     * Get all applications for a specific job
     */
    fun getApplicationsForJob(jobId: String): List<JobApplication> {
        return _applications.value.filter { it.jobId == jobId && it.status == ApplicationStatus.PENDING }
    }

    /**
     * Select a worker for a job
     */
    fun selectWorker(jobId: String, workerId: String, jobTitle: String = "Job", jobRepository: com.demoapp.feature_jobs.data.JobRepository? = null): Boolean {
        val currentApplications = _applications.value.toMutableList()
        val applicationIndex = currentApplications.indexOfFirst { 
            it.jobId == jobId && it.workerId == workerId 
        }
        
        if (applicationIndex != -1) {
            val selectedApplication = currentApplications[applicationIndex]
            
            // Mark this application as selected
            currentApplications[applicationIndex] = selectedApplication.copy(
                status = ApplicationStatus.SELECTED,
                selectedAt = java.util.Date()
            )
            
            // Reject all other applications for this job
            val rejectedApplications = mutableListOf<JobApplication>()
            currentApplications.forEachIndexed { index, app ->
                if (app.jobId == jobId && app.workerId != workerId && app.status == ApplicationStatus.PENDING) {
                    currentApplications[index] = app.copy(status = ApplicationStatus.REJECTED)
                    rejectedApplications.add(app)
                }
            }
            
            _applications.value = currentApplications
            
            // Update job status to IN_PROGRESS and assign worker
            jobRepository?.let { repo ->
                val job = repo.getJobById(jobId)
                if (job != null) {
                    // Assign worker to job and update status to IN_PROGRESS
                    repo.assignWorkerToJob(jobId, workerId)
                    println("DEBUG: Job $jobId assigned to worker $workerId, status updated to IN_PROGRESS")
                }
            }
            
            // Create notifications
            val notificationRepository = NotificationRepository.getInstance()
            
            // Notify selected worker
            notificationRepository.createJobSelectionNotification(
                jobId = jobId,
                jobTitle = jobTitle,
                workerId = workerId,
                workerName = selectedApplication.workerName
            )
            
            // Create job start notification for worker
            notificationRepository.createJobStartNotification(
                jobId = jobId,
                jobTitle = jobTitle,
                workerId = workerId,
                workerName = selectedApplication.workerName
            )
            
            // Notify rejected workers
            rejectedApplications.forEach { rejectedApp ->
                notificationRepository.createJobRejectionNotification(
                    jobId = jobId,
                    jobTitle = jobTitle,
                    workerId = rejectedApp.workerId
                )
            }
            
            return true
        }
        return false
    }

    /**
     * Reject a worker application
     */
    fun rejectWorker(jobId: String, workerId: String): Boolean {
        val currentApplications = _applications.value.toMutableList()
        val applicationIndex = currentApplications.indexOfFirst { 
            it.jobId == jobId && it.workerId == workerId 
        }
        
        if (applicationIndex != -1) {
            currentApplications[applicationIndex] = currentApplications[applicationIndex].copy(
                status = ApplicationStatus.REJECTED
            )
            _applications.value = currentApplications
            return true
        }
        return false
    }

    /**
     * Get selected worker for a job
     */
    fun getSelectedWorker(jobId: String): JobApplication? {
        return _applications.value.find { 
            it.jobId == jobId && it.status == ApplicationStatus.SELECTED 
        }
    }

    /**
     * Initialize with sample data for demo purposes
     */
    fun initializeSampleData() {
        val sampleApplications = listOf(
            JobApplication(
                id = "app_1",
                jobId = "sample_grocery_1", // Grocery Shopping job
                workerId = "worker_1",
                workerName = "John Mwangi",
                workerPhone = "+254712345678",
                workerRating = 4.8f,
                workerCompletedTasks = 45,
                applicationMessage = "I have experience with shopping tasks and I'm available immediately."
            ),
            JobApplication(
                id = "app_2",
                jobId = "sample_grocery_1", // Same grocery job
                workerId = "worker_2",
                workerName = "Sarah Wanjiku",
                workerPhone = "+254723456789",
                workerRating = 4.6f,
                workerCompletedTasks = 32,
                applicationMessage = "I live near the area and can complete this task quickly."
            ),
            JobApplication(
                id = "app_3",
                jobId = "sample_delivery_1", // Package Delivery job
                workerId = "worker_3",
                workerName = "Peter Kimani",
                workerPhone = "+254734567890",
                workerRating = 4.9f,
                workerCompletedTasks = 67,
                applicationMessage = "I have a motorcycle and can handle delivery tasks efficiently."
            ),
            JobApplication(
                id = "app_4",
                jobId = "sample_delivery_1", // Same delivery job
                workerId = "worker_4",
                workerName = "Grace Akinyi",
                workerPhone = "+254745678901",
                workerRating = 4.7f,
                workerCompletedTasks = 28,
                applicationMessage = "I'm reliable and have good communication skills."
            ),
            JobApplication(
                id = "app_5",
                jobId = "sample_survey_1", // Customer Survey job
                workerId = "worker_5",
                workerName = "David Ochieng",
                workerPhone = "+254756789012",
                workerRating = 4.5f,
                workerCompletedTasks = 19,
                applicationMessage = "I'm good with surveys and have excellent communication skills."
            ),
            JobApplication(
                id = "app_6",
                jobId = "sample_cleaning_1", // House Cleaning job
                workerId = "worker_6",
                workerName = "Mary Wanjiku",
                workerPhone = "+254767890123",
                workerRating = 4.8f,
                workerCompletedTasks = 52,
                applicationMessage = "I have 3 years of cleaning experience and bring my own supplies."
            ),
            JobApplication(
                id = "app_7",
                jobId = "sample_tech_1", // Tech Support job
                workerId = "worker_7",
                workerName = "James Otieno",
                workerPhone = "+254778901234",
                workerRating = 4.9f,
                workerCompletedTasks = 38,
                applicationMessage = "I'm a certified IT technician and can help with WiFi setup."
            )
        )
        
        _applications.value = sampleApplications
    }
}
