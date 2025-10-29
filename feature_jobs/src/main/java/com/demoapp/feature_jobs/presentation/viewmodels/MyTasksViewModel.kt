package com.demoapp.feature_jobs.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.core_network.models.TaskData as ApiTaskData
import com.demoapp.feature_jobs.data.TaskRepository
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

    init {
        loadMyTasks()
    }

    fun loadMyTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = taskRepository.getMyTasks()
                
                result.fold(
                    onSuccess = { response ->
                        val tasks = response.data?.map { apiTask ->
                            mapApiTaskToJobData(apiTask)
                        } ?: emptyList()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tasks = tasks,
                            activeTasks = tasks.filter { it.status == JobStatus.ACTIVE || it.status == JobStatus.IN_PROGRESS },
                            appliedTasks = tasks.filter { it.status == JobStatus.APPLIED },
                            completedTasks = tasks.filter { it.status == JobStatus.COMPLETED },
                            cancelledTasks = tasks.filter { it.status == JobStatus.CANCELLED },
                            error = null
                        )
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

    private fun mapApiTaskToJobData(apiTask: ApiTaskData): JobData {
        return JobData(
            id = apiTask.id.toString(),
            title = apiTask.title,
            description = apiTask.task_description,
            pay = apiTask.budget_kes,
            deadline = formatDueDate(apiTask.due_date),
            jobType = apiTask.category,
            location = apiTask.store_service_location,
            status = mapTaskStatus(apiTask),
            deliveryAddress = apiTask.delivery_location,
            deliveryLat = apiTask.delivery_latitude,
            deliveryLng = apiTask.delivery_longitude,
            distance = 0.0, // TODO: Calculate distance if needed
            workerId = null, // Will be set when worker applies
            workerAccepted = false,
            invoiceCreated = false,
            cancellationReason = null
        )
    }

    private fun mapTaskStatus(apiTask: ApiTaskData): JobStatus {
        // For now, map all API tasks to ACTIVE status
        // In a real implementation, you might have status field in the API response
        return JobStatus.ACTIVE
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
