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
            val timelineId = "timeline_${System.currentTimeMillis()}"
            
            currentJobs[jobIndex] = job.copy(
                workerId = workerId,
                status = JobStatus.IN_PROGRESS,
                workerAccepted = true,
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

    fun updateJobTimelineStage(jobId: String, stage: com.demoapp.feature_jobs.presentation.models.TimelineStage) {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                currentTimelineStage = stage,
                isCompleted = stage == com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED,
                completedAt = if (stage == com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED) java.util.Date() else null
            )
            _jobs.value = currentJobs
        }
    }

    fun createInvoiceForJob(jobId: String, invoiceId: String): Boolean {
        val currentJobs = _jobs.value.toMutableList()
        val jobIndex = currentJobs.indexOfFirst { it.id == jobId }
        if (jobIndex != -1 && !currentJobs[jobIndex].invoiceCreated) {
            currentJobs[jobIndex] = currentJobs[jobIndex].copy(
                invoiceCreated = true,
                invoiceId = invoiceId
            )
            _jobs.value = currentJobs
            return true
        }
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
            ),
            JobData(
                id = "sample_survey_1",
                title = "Customer Survey",
                description = "Conduct survey at the mall",
                pay = 20.0,
                distance = 1.8,
                deadline = "Today, 8:00 PM",
                jobType = "Survey",
                clientId = "client_sarah_brown", // Different client ID
                clientPhoneNumber = "+254734567890",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null,
                // Location data
                latitude = -6.1780,
                longitude = 35.7460,
                locationName = "Dodoma Mall",
                targetArea = "Dodoma Central Mall - Customer Survey Area",
                surveyLat = -6.1780,
                surveyLng = 35.7460,
                surveyQuestions = "Customer satisfaction survey - 10 questions"
            ),
            JobData(
                id = "sample_cleaning_1",
                title = "House Cleaning",
                description = "Clean a 2-bedroom apartment",
                pay = 45.0,
                distance = 3.1,
                deadline = "Today, 4:00 PM",
                jobType = "Cleaning",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null,
                // Location data
                latitude = -6.1690,
                longitude = 35.7370,
                locationName = "Downtown Apartments",
                deliveryAddress = "321 Apartment Complex, Dodoma, Tanzania",
                deliveryLat = -6.1690,
                deliveryLng = 35.7370
            ),
            JobData(
                id = "sample_pet_1",
                title = "Pet Walking",
                description = "Walk a friendly dog for 30 minutes",
                pay = 15.0,
                distance = 1.2,
                deadline = "Today, 7:00 PM",
                jobType = "Pet Care",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            ),
            JobData(
                id = "sample_driver_1",
                title = "Driver Needed",
                description = "Drive client to airport and back",
                pay = 75.0,
                distance = 12.5,
                deadline = "Tomorrow, 9:00 AM",
                jobType = "Driver",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            ),
            JobData(
                id = "sample_tech_1",
                title = "Tech Support",
                description = "Help set up home WiFi network",
                pay = 30.0,
                distance = 4.0,
                deadline = "Today, 5:00 PM",
                jobType = "Tech Support",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            ),
            JobData(
                id = "sample_tutoring_1",
                title = "Math Tutoring",
                description = "Help with high school math homework",
                pay = 40.0,
                distance = 2.0,
                deadline = "Tomorrow, 3:00 PM",
                jobType = "Tutoring",
                status = JobStatus.ACTIVE,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            ),
            // Completed jobs for testing invoice creation
            JobData(
                id = "completed_grocery_1",
                title = "Grocery Shopping - Completed",
                description = "Successfully completed grocery shopping task",
                pay = 30.0,
                distance = 2.0,
                deadline = "Yesterday, 6:00 PM",
                jobType = "Shopping",
                status = JobStatus.COMPLETED,
                workerAccepted = true,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            ),
            JobData(
                id = "completed_delivery_1",
                title = "Package Delivery - Completed",
                description = "Successfully delivered package to client",
                pay = 40.0,
                distance = 3.5,
                deadline = "Yesterday, 2:00 PM",
                jobType = "Delivery",
                status = JobStatus.COMPLETED,
                workerAccepted = true,
                invoiceCreated = true,
                invoiceId = "INV-001",
                cancellationReason = null
            ),
            JobData(
                id = "completed_survey_1",
                title = "Customer Survey - Completed",
                description = "Successfully conducted customer survey",
                pay = 25.0,
                distance = 1.5,
                deadline = "Yesterday, 8:00 PM",
                jobType = "Survey",
                status = JobStatus.COMPLETED,
                workerAccepted = true,
                invoiceCreated = false,
                invoiceId = null,
                cancellationReason = null
            )
        )
        _jobs.value = sampleJobs
    }
}

object JobRepositorySingleton {
    val instance = JobRepository()
}
