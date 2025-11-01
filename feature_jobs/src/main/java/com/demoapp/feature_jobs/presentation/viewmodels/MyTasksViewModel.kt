package com.demoapp.feature_jobs.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.core_network.models.TaskData as ApiTaskData
import com.demoapp.feature_jobs.data.TaskRepository
import com.demoapp.feature_jobs.data.JobRepositorySingleton
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class MyTasksUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tasks: List<JobData> = emptyList(),
    val activeTasks: List<JobData> = emptyList(),
    val appliedTasks: List<JobData> = emptyList(),
    val completedTasks: List<JobData> = emptyList(),
    val cancelledTasks: List<JobData> = emptyList()
)

class MyTasksViewModel(
    private val context: Context
) : ViewModel() {

    private val taskRepository = TaskRepository.getInstance(context)
    
    private val _uiState = MutableStateFlow(MyTasksUiState())
    val uiState: StateFlow<MyTasksUiState> = _uiState.asStateFlow()

    init { loadMyTasks() }

    fun loadMyTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = taskRepository.getMyTasks()
                
                result.fold(
                    onSuccess = { response ->
                        val backendTasks = response.data?.map { apiTask ->
                            mapApiTaskToJobData(apiTask)
                        } ?: emptyList()
                        
                        // Also get ALL local jobs that are completed (in case backend hasn't updated yet)
                        val localJobRepository = JobRepositorySingleton.instance
                        val allLocalJobs = localJobRepository.jobs.value
                        android.util.Log.d("MyTasksViewModel", "Total local jobs in repository: ${allLocalJobs.size}")
                        
                        val localCompletedJobs = allLocalJobs
                            .filter { 
                                // Include jobs that have invoice created OR are marked as completed
                                val isCompleted = (it.invoiceCreated == true) || 
                                                (it.status == JobStatus.COMPLETED || it.isCompleted == true)
                                if (isCompleted) {
                                    android.util.Log.d("MyTasksViewModel", "Found local completed job: id=${it.id}, title=${it.title}, status=${it.status}, invoiceCreated=${it.invoiceCreated}, isCompleted=${it.isCompleted}")
                                }
                                isCompleted
                            }
                        
                        android.util.Log.d("MyTasksViewModel", "Found ${localCompletedJobs.size} local completed jobs out of ${allLocalJobs.size} total local jobs")
                        
                        // Merge backend and local jobs - prioritize local completed versions
                        // First, collect all unique job IDs
                        val allJobIds = (backendTasks.map { it.id } + allLocalJobs.map { it.id }).distinct()
                        val allTasks = allJobIds.map { jobId ->
                            // First check local repository for this job
                            val localJob = allLocalJobs.find { it.id == jobId }
                            val backendJob = backendTasks.find { it.id == jobId }
                            
                            // If local job is completed or has invoice, always use it
                            if (localJob != null && 
                                (localJob.invoiceCreated == true || 
                                 localJob.status == JobStatus.COMPLETED || 
                                 localJob.isCompleted == true)) {
                                android.util.Log.d("MyTasksViewModel", "Using local completed job ${jobId}: status=${localJob.status}, invoiceCreated=${localJob.invoiceCreated}")
                                localJob
                            } else if (backendJob != null) {
                                // Use backend job if local doesn't exist or isn't completed
                                backendJob
                            } else if (localJob != null) {
                                // Use local job if no backend version
                                localJob
                            } else {
                                // This shouldn't happen, but handle gracefully
                                android.util.Log.w("MyTasksViewModel", "Job $jobId not found in either local or backend")
                                null
                            }
                        }.filterNotNull()
                        
                        android.util.Log.d("MyTasksViewModel", "Total merged tasks: ${allTasks.size}, backend: ${backendTasks.size}, local completed: ${localCompletedJobs.size}, all local: ${allLocalJobs.size}")
                        
                        val finalAllTasks = allTasks
                        
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                tasks = finalAllTasks,
                                activeTasks = finalAllTasks.filter { it.status == JobStatus.ACTIVE || it.status == JobStatus.IN_PROGRESS },
                                appliedTasks = finalAllTasks.filter { it.status == JobStatus.APPLIED },
                                completedTasks = finalAllTasks.filter { 
                                    // Include jobs with invoice created OR marked as completed
                                    val isCompleted = (it.invoiceCreated == true) || 
                                                    (it.status == JobStatus.COMPLETED || it.isCompleted == true)
                                    if (isCompleted) {
                                        android.util.Log.d("MyTasksViewModel", "Including completed job: id=${it.id}, title=${it.title}, status=${it.status}, invoiceCreated=${it.invoiceCreated}, isCompleted=${it.isCompleted}")
                                    }
                                    isCompleted
                                },
                                cancelledTasks = finalAllTasks.filter { it.status == JobStatus.CANCELLED },
                                error = null
                            )
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tasks = finalAllTasks,
                            activeTasks = finalAllTasks.filter { it.status == JobStatus.ACTIVE || it.status == JobStatus.IN_PROGRESS },
                            appliedTasks = finalAllTasks.filter { it.status == JobStatus.APPLIED },
                            completedTasks = finalAllTasks.filter { 
                                // Include jobs with invoice created OR marked as completed
                                val isCompleted = (it.invoiceCreated == true) || 
                                                (it.status == JobStatus.COMPLETED || it.isCompleted == true)
                                if (isCompleted) {
                                    android.util.Log.d("MyTasksViewModel", "Including completed job in UI: id=${it.id}, title=${it.title}, status=${it.status}, invoiceCreated=${it.invoiceCreated}, isCompleted=${it.isCompleted}")
                                }
                                isCompleted
                            },
                            cancelledTasks = finalAllTasks.filter { it.status == JobStatus.CANCELLED },
                            error = null
                        )
                        
                        android.util.Log.d("MyTasksViewModel", "Final completedTasks count: ${_uiState.value.completedTasks.size}")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load tasks"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshTasks() {
        loadMyTasks()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    suspend fun cancelTask(taskId: String): Result<Unit> {
        val result = taskRepository.cancelTask(taskId)
        return result.fold(
            onSuccess = {
                // reload tasks after cancel
                loadMyTasks()
                Result.success(Unit)
            },
            onFailure = { e -> Result.failure(e) }
        )
    }

    suspend fun createInvoice(taskId: String): Result<String> {
        val result = taskRepository.createInvoice(taskId)
        return result.fold(
            onSuccess = { resp -> Result.success(resp.message) },
            onFailure = { e -> Result.failure(e) }
        )
    }

    private fun mapApiTaskToJobData(apiTask: ApiTaskData): JobData {
        // Since TaskData from MyTasksResponse doesn't have status field,
        // we'll default to IN_PROGRESS and let local repository override it
        // Check local repository first for more accurate status
        val localJobRepository = JobRepositorySingleton.instance
        val localJob = localJobRepository.jobs.value.find { it.id == apiTask.id.toString() }
        
        // Use local job status if available and it's more complete (e.g., COMPLETED)
        val jobStatus = if (localJob != null && (localJob.status == JobStatus.COMPLETED || localJob.invoiceCreated)) {
            localJob.status
        } else {
            // Default to IN_PROGRESS for tasks assigned to worker
            JobStatus.IN_PROGRESS
        }
        
        val isCompleted = jobStatus == JobStatus.COMPLETED || (localJob?.isCompleted == true) || (localJob?.invoiceCreated == true)
        
        return JobData(
            id = apiTask.id.toString(),
            title = apiTask.title,
            description = apiTask.task_description,
            pay = apiTask.budget_kes,
            deadline = formatDueDate(apiTask.due_date),
            jobType = apiTask.category,
            location = apiTask.store_service_location,
            status = jobStatus,
            deliveryAddress = apiTask.delivery_location,
            deliveryLat = apiTask.delivery_latitude,
            deliveryLng = apiTask.delivery_longitude,
            distance = 0.0,
            workerId = localJob?.workerId, // Use local job workerId if available
            workerAccepted = localJob?.workerAccepted ?: false,
            invoiceCreated = localJob?.invoiceCreated ?: false,
            cancellationReason = null,
            isCompleted = isCompleted,
            currentTimelineStage = localJob?.currentTimelineStage ?: 
                (if (isCompleted) com.demoapp.feature_jobs.presentation.models.TimelineStage.JOB_COMPLETED else null)
        )
    }

    private fun formatDueDate(dueDate: String): String {
        return try {
            val instant = Instant.parse(dueDate)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            dueDate // Return original string if parsing fails
        }
    }
}
