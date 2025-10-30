package com.demoapp.feature_auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demoapp.core_network.models.LoginResponse
import com.demoapp.core_network.models.RegisterResponse
import com.demoapp.feature_auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: UserData? = null,
    val token: String? = null
)

data class UserData(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val comments: String? = null,
    val profilePhoto: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String,
        passwordConfirm: String,
        age: Int? = null,
        gender: String? = null,
        address: String? = null,
        comments: String? = null,
        profilePhoto: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = authRepository.register(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phoneNumber = phoneNumber,
                password = password,
                passwordConfirm = passwordConfirm,
                age = age,
                gender = gender,
                address = address,
                comments = comments,
                profilePhoto = profilePhoto
            )

            result.fold(
                onSuccess = { registerResponse ->
                    val userData = registerResponse.data?.user?.let { user ->
                        UserData(
                            id = user.id,
                            firstName = user.first_name,
                            lastName = user.last_name,
                            email = user.email ?: "",
                            phoneNumber = user.phone_number,
                            age = user.age,
                            gender = user.gender,
                            address = user.address,
                            comments = user.comments,
                            profilePhoto = user.profile_photo
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = userData,
                        token = registerResponse.data?.tokens?.access,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun login(
        phoneNumber: String,
        password: String
    ) {
        android.util.Log.d("AuthViewModel", "login() called with phoneNumber: $phoneNumber")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            android.util.Log.d("AuthViewModel", "Calling authRepository.login()")
            val result = authRepository.login(
                phoneNumber = phoneNumber,
                password = password
            )
            
            android.util.Log.d("AuthViewModel", "Repository call completed, processing result...")

            result.fold(
                onSuccess = { loginResponse ->
                    android.util.Log.d("AuthViewModel", "Login response received - success: ${loginResponse.success}, data: ${loginResponse.data != null}")
                    android.util.Log.d("AuthViewModel", "Login response tokens: ${loginResponse.data?.tokens != null}")
                    android.util.Log.d("AuthViewModel", "Login response access token: ${loginResponse.data?.tokens?.access != null}")
                    
                    val userData = loginResponse.data?.user?.let { user ->
                        UserData(
                            id = user.id,
                            firstName = user.first_name,
                            lastName = user.last_name,
                            email = user.email ?: "",
                            phoneNumber = user.phone_number,
                            age = user.age,
                            gender = user.gender,
                            address = user.address,
                            comments = user.comments,
                            profilePhoto = user.profile_photo
                        )
                    }

                    val accessToken = loginResponse.data?.tokens?.access
                    android.util.Log.d("AuthViewModel", "Setting state - isSuccess: true, token: ${if (accessToken != null) "Present (${accessToken.take(20)}...)" else "NULL"}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = userData,
                        token = accessToken,
                        error = null
                    )
                },
                onFailure = { exception ->
                    android.util.Log.e("AuthViewModel", "Login failed: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout(token: String? = null) {
        viewModelScope.launch {
            val currentToken = token ?: _uiState.value.token
            android.util.Log.d("AuthViewModel", "logout() called - token present: ${currentToken != null}")
            if (currentToken != null) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                android.util.Log.d("AuthViewModel", "Calling authRepository.logout()")
                val result = authRepository.logout(currentToken)
                
                result.fold(
                    onSuccess = {
                        android.util.Log.d("AuthViewModel", "Logout successful - clearing state")
                        // Clear the state on successful logout
                        _uiState.value = AuthState()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("AuthViewModel", "Logout failed: ${exception.message}", exception)
                        // Even if logout fails on server, clear local state
                        _uiState.value = AuthState()
                    }
                )
            } else {
                android.util.Log.d("AuthViewModel", "No token to logout - clearing state")
                // No token to logout, just clear state
                _uiState.value = AuthState()
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthState()
    }
}
