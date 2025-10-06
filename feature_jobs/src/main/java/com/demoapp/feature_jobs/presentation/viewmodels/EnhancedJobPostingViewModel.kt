package com.demoapp.feature_jobs.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.feature_jobs.data.OfflineJobRepository
import com.demoapp.feature_jobs.data.NetworkConnectivityManager
import com.demoapp.feature_jobs.data.JobCreationResult
import com.demoapp.feature_jobs.data.JobCancellationResult
import com.demoapp.feature_jobs.presentation.models.JobData
import com.demoapp.feature_jobs.presentation.models.JobStatus
import com.demoapp.feature_jobs.presentation.models.CancellationReasonType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for enhanced job posting functionality
 * Follows MVVM + Clean Architecture principles
 */
class EnhancedJobPostingViewModel(
    private val repository: OfflineJobRepository,
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobPostingUiState())
    val uiState: StateFlow<JobPostingUiState> = _uiState.asStateFlow()

    private val _drafts = MutableStateFlow<List<JobData>>(emptyList())
    val drafts: StateFlow<List<JobData>> = _drafts.asStateFlow()

    private val _networkStatus = MutableStateFlow(true)
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    init {
        // Start monitoring network connectivity
        networkManager.startMonitoring()
        
        // Observe network status
        viewModelScope.launch {
            networkManager.isConnected.collect { isConnected ->
                _networkStatus.value = isConnected
                repository.updateNetworkStatus(isConnected)
                
                // Auto-sync drafts when network is restored
                if (isConnected) {
                    syncDrafts()
                }
            }
        }

        // Observe drafts
        viewModelScope.launch {
            repository.drafts.collect { draftList ->
                _drafts.value = draftList
            }
        }
    }

    fun createJob(jobData: JobData, isDraft: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = if (isDraft || !_networkStatus.value) {
                    repository.saveAsDraft(jobData)
                } else {
                    repository.addJob(jobData)
                }
                
                when (result) {
                    is JobCreationResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isJobCreated = true,
                            createdJob = result.job
                        )
                    }
                    is JobCreationResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.errors.joinToString("\n")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun cancelJob(jobId: String, reason: String, reasonType: CancellationReasonType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.cancelJob(jobId, reason, reasonType)
                
                when (result) {
                    is JobCancellationResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isJobCancelled = true,
                            cancelledJob = result.job,
                            cancellationPenalty = result.penalty
                        )
                    }
                    is JobCancellationResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun syncDrafts() {
        viewModelScope.launch {
            try {
                val syncedJobs = repository.syncDrafts()
                if (syncedJobs.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        syncedDraftsCount = syncedJobs.size
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to sync drafts: ${e.message}"
                )
            }
        }
    }

    fun getJobById(jobId: String): JobData? {
        return repository.getJobById(jobId)
    }

    fun getJobsWithWeightWarnings(): List<JobData> {
        return repository.getJobsWithWeightWarnings()
    }

    fun getLateJobs(): List<JobData> {
        return repository.getLateJobs()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetJobCreationState() {
        _uiState.value = _uiState.value.copy(
            isJobCreated = false,
            createdJob = null
        )
    }

    fun resetCancellationState() {
        _uiState.value = _uiState.value.copy(
            isJobCancelled = false,
            cancelledJob = null,
            cancellationPenalty = 0.0
        )
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.stopMonitoring()
    }
}

data class JobPostingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isJobCreated: Boolean = false,
    val createdJob: JobData? = null,
    val isJobCancelled: Boolean = false,
    val cancelledJob: JobData? = null,
    val cancellationPenalty: Double = 0.0,
    val syncedDraftsCount: Int = 0
)
