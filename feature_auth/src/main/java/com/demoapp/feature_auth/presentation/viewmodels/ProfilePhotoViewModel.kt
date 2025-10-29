package com.demoapp.feature_auth.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.core_network.models.UserProfileResponse
import com.demoapp.feature_auth.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUploadState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfileResponse? = null,
    val selectedImageUri: Uri? = null
)

class ProfilePhotoViewModel : ViewModel() {
    
    private val profileRepository = ProfileRepository()
    
    private val _uiState = MutableStateFlow(ProfileUploadState())
    val uiState: StateFlow<ProfileUploadState> = _uiState.asStateFlow()
    
    fun setSelectedImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            error = null
        )
    }
    
    fun uploadProfilePhoto(
        firstName: String,
        lastName: String,
        email: String,
        authToken: String,
        context: Context
    ) {
        val selectedUri = _uiState.value.selectedImageUri
        if (selectedUri == null) {
            _uiState.value = _uiState.value.copy(
                error = "Please select an image first"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            val result = profileRepository.uploadProfilePhoto(
                firstName = firstName,
                lastName = lastName,
                email = email,
                authToken = authToken,
                imageUri = selectedUri,
                context = context
            )
            
            result.fold(
                onSuccess = { userProfile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        userProfile = userProfile,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Upload failed"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetState() {
        _uiState.value = ProfileUploadState()
    }
}
