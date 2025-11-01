package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JobRepository {
    private val _jobs = MutableStateFlow<List<JobData>>(emptyList())
    val jobs: StateFlow<List<JobData>> = _jobs.asStateFlow()

    fun addJob(job: JobData) {
        val currentJobs = _jobs.value.toMutableList()
        val jobWithId = job.copy(
            id = if (job.id.isEmpty()) "job_${System.currentTimeMillis()}" else job.id,
            status = JobStatus.ACTIVE
        )
        currentJobs.add(jobWithId)
        _jobs.value = currentJobs
    }

    fun acceptJob(job: JobData) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == job.id }
        if (jobIndex != -1) {
            // Update the existing job to show it's been applied by a worker
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                status = JobStatus.APPLIED,
                workerAccepted = true
            )
            _jobs.value = currentJobs
        }
    }

    fun updateJobStatus(jobId: String, status: JobStatus) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(status = status)
            _jobs.value = currentJobs
        }
    }

    fun assignWorkerToJob(jobId: String, workerId: String) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            val job = currentJobs[jobIndex]
            
            // When assigning worker, set workerId but DON'T set workerAccepted yet
            // Worker must accept the job explicitly in the chat
            currentJobs[jobIndex] = job.copy(
                workerId = workerId,
                status = JobStatus.ACTIVE, // Keep as ACTIVE until worker accepts
                workerAccepted = false, // Worker needs to accept explicitly
                currentTimelineStage = null, // No timeline stage until worker accepts
                timelineId = null
            )
            _jobs.value = currentJobs
        }
    }
    
    /**
     * Worker accepts the job (called from chat screen after worker confirms)
     */
    fun acceptJob(jobId: String, workerId: String) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            val job = currentJobs[jobIndex]
            val timelineId = "timeline_${System.currentTimeMillis()}"
            
            // Only update if the workerId matches
            if (job.workerId == workerId || job.workerId?.contains(workerId) == true || workerId.contains(job.workerId ?: "")) {
                currentJobs[jobIndex] = job.copy(
                    workerAccepted = true,
                    status = JobStatus.IN_PROGRESS,
                    currentTimelineStage = com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_ACCEPTED,
                    timelineId = timelineId
                )
                _jobs.value = currentJobs
                
                // Create timeline for this job
                val timelineRepository = JobTimelineRepository.getInstance()
                timelineRepository.createTimelineForJob(
                    jobId = jobId,
                    workerId = workerId,
                    workerName = "Worker Name", // This should come from worker data
                    clientId = job.clientId,
                    clientName = "Client Name" // This should come from client data
                )
            }
        }
    }

    fun updateJobTimelineStage(jobId: String, stage: com.demoapp.feature_jobs.presentation.models.TimelineStage) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            val job = currentJobs[jobIndex]
            currentJobs[jobIndex] = job.copy(
                currentTimelineStage = stage,
                isCompleted = stage == com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED,
                completedAt = if (stage == com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED) java.util.Date() else null,
                // If setting to JOB_COMPLETED, also update status to COMPLETED
                status = if (stage == com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED) 
                    JobStatus.COMPLETED 
                else 
                    job.status
            )
            _jobs.value = currentJobs
        }
    }
    
    fun updateJob(job: JobData) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == job.id }
        if (jobIndex != -1) {
            currentJobs[jobIndex] = job
            android.util.Log.d("JobRepository", "Updated existing job ${job.id}: status=${job.status}, invoiceCreated=${job.invoiceCreated}, isCompleted=${job.isCompleted}")
        } else {
            // Upsert: add if not present
            currentJobs.add(job)
            android.util.Log.d("JobRepository", "Added new job ${job.id}: status=${job.status}, invoiceCreated=${job.invoiceCreated}, isCompleted=${job.isCompleted}")
        }
        _jobs.value = currentJobs
        android.util.Log.d("JobRepository", "Total jobs in repository after update: ${_jobs.value.size}")
    }

    fun createInvoiceForJob(jobId: String, invoiceId: String): Boolean {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            val existingJob = currentJobs[jobIndex]
            currentJobs[jobIndex] = existingJob.copy(
                invoiceCreated = true,
                invoiceId = invoiceId,
                status = JobStatus.COMPLETED, // Also mark as completed when invoice is created
                isCompleted = true,
                currentTimelineStage = com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED,
                completedAt = java.util.Date()
            )
            _jobs.value = currentJobs
            android.util.Log.d("JobRepository", "Invoice created for job $jobId, status set to COMPLETED")
            return true
        }
        android.util.Log.w("JobRepository", "Job not found for invoice creation: $jobId")
        return false
    }

    fun cancelJob(jobId: String, reason: String): Boolean {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1 && 
            (currentJobs[jobIndex].status == JobStatus.ACTIVE || 
             currentJobs[jobIndex].status == JobStatus.IN_PROGRESS)) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                status = JobStatus.CANCELLED,
                cancellationReason = reason
            )
            _jobs.value = currentJobs
            return true
        }
        return false
    }

    fun cancelJobByTitle(jobTitle: String, reason: String): Boolean {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.title == jobTitle }
        if (jobIndex != -1 && 
            (currentJobs[jobIndex].status == JobStatus.ACTIVE || 
             currentJobs[jobIndex].status == JobStatus.IN_PROGRESS)) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                status = JobStatus.CANCELLED,
                cancellationReason = reason
            )
            _jobs.value = currentJobs
            return true
        }
        return false
    }

    fun getJobById(jobId: String): JobData? {
        return _jobs.value.find { it.id == jobId }
    }

    fun getJobByTitle(jobTitle: String): JobData? {
        return _jobs.value.find { it.title == jobTitle }
    }

    fun initializeSampleData() {
        // Keep only 2 sample jobs - rest should come from backend
        val sampleJobs = listOf(
            JobData(
                id = "sample_grocery_1",
                title = "Grocery Shopping",
                description = "Buy groceries from the local market",
                pay = 25.0,
                distance = 2.5,
                deadline = "Today, 6:00 PM",
                jobType = "Shopping",
                clientId = "client_mary_johnson", // Specific client ID
                clientPhoneNumber = "+254712345678",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null,
                // Location data
                latitude = -6.2088,
                longitude = 35.7395,
                locationName = "Dodoma Central Market",
                deliveryAddress = "123 Main Street, Dodoma, Tanzania",
                deliveryLat = -6.1730,
                deliveryLng = 35.7419,
                shoppingList = "Milk, Bread, Eggs, Fruits, Vegetables"
            ),
            JobData(
                id = "sample_delivery_1",
                title = "Package Delivery",
                description = "Deliver package from downtown",
                pay = 35.0,
                distance = 5.2,
                deadline = "Tomorrow, 2:00 PM",
                jobType = "Delivery",
                clientId = "client_david_wilson", // Different client ID
                clientPhoneNumber = "+254723456789",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null,
                // Location data
                latitude = -6.1630,
                longitude = 35.7516,
                locationName = "Dodoma Post Office",
                pickupAddress = "456 Post Office Road, Dodoma, Tanzania",
                pickupLat = -6.1630,
                pickupLng = 35.7516,
                dropoffAddress = "789 Client Avenue, Dodoma, Tanzania",
                dropoffLat = -6.1730,
                dropoffLng = 35.7419,
                parcelDescription = "Small electronics package, fragile"
            )
        )
        _jobs.value = sampleJobs
    }
}

object JobRepositorySingleton {
    val instance = JobRepository()
}
