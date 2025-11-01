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
     * Submit a new application (used by ContractorApplicationScreen)
     */
    fun submitApplication(application: JobApplication) {
        val currentApplications = _applications.value.toMutableList()
        currentApplications.add(application)
        _applications.value = currentApplications
        
        // Create notification for the job poster
        val notificationRepository = NotificationRepository.getInstance()
        notificationRepository.createJobApplicationNotification(
            jobId = application.jobId,
            jobTitle = "Job Application", // This should come from job data
            clientId = "client_placeholder", // This should come from job data
            workerId = application.workerId,
            workerName = application.workerName
        )
    }

    /**
     * Initialize with sample data for demo purposes
     */
    fun initializeSampleData() {
        // Keep only 1-2 sample applicants - rest should come from backend
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
                jobId = "sample_delivery_1", // Package Delivery job
                workerId = "worker_2",
                workerName = "Sarah Wanjiku",
                workerPhone = "+254723456789",
                workerRating = 4.6f,
                workerCompletedTasks = 32,
                applicationMessage = "I have a motorcycle and can handle delivery tasks efficiently."
            )
        )
        
        _applications.value = sampleApplications
    }

    /**
     * Withdraw a job application (contractor cancels their application)
     */
    fun withdrawApplication(jobId: String, workerId: String): Boolean {
        val currentApplications = _applications.value.toMutableList()
        val applicationIndex = currentApplications.indexOfFirst { 
            it.jobId == jobId && it.workerId == workerId 
        }
        
        if (applicationIndex != -1) {
            val application = currentApplications[applicationIndex]
            
            // Only allow withdrawal if application is still pending
            if (application.status == ApplicationStatus.PENDING) {
                currentApplications[applicationIndex] = application.copy(
                    status = ApplicationStatus.WITHDRAWN,
                    withdrawnAt = java.util.Date()
                )
                _applications.value = currentApplications
                return true
            }
        }
        return false
    }

    /**
     * Get applications by worker ID
     */
    fun getApplicationsByWorker(workerId: String): List<JobApplication> {
        return _applications.value.filter { it.workerId == workerId }
    }

    /**
     * Get pending applications by worker ID
     */
    fun getPendingApplicationsByWorker(workerId: String): List<JobApplication> {
        return _applications.value.filter { 
            it.workerId == workerId && it.status == ApplicationStatus.PENDING 
        }
    }

    /**
     * Check if a contractor is selected for a specific job
     */
    fun isContractorSelectedForJob(jobId: String, workerId: String): Boolean {
        return _applications.value.any { 
            it.jobId == jobId && it.workerId == workerId && it.status == ApplicationStatus.SELECTED 
        }
    }

    /**
     * Get selected applications by worker ID
     */
    fun getSelectedApplicationsByWorker(workerId: String): List<JobApplication> {
        return _applications.value.filter { 
            it.workerId == workerId && it.status == ApplicationStatus.SELECTED 
        }
    }

    /**
     * Update application status
     */
    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        val currentApplications = _applications.value.toMutableList()
        val applicationIndex = currentApplications.indexOfFirst { it.id == applicationId }
        
        if (applicationIndex != -1) {
            currentApplications[applicationIndex] = currentApplications[applicationIndex].copy(status = newStatus)
            _applications.value = currentApplications
        }
    }
}
