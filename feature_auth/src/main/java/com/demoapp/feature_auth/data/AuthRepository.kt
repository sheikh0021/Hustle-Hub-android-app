package com.demoapp.feature_auth.data

import com.demoapp.core_network.NetworkClient
import com.demoapp.core_network.models.LoginRequest
import com.demoapp.core_network.models.LoginResponse
import com.demoapp.core_network.models.RegisterRequest
import com.demoapp.core_network.models.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository {

    private val authApi = NetworkClient.authApi

    suspend fun register(
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
    ): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                first_name = firstName,
                last_name = lastName,
                email = email,
                phone_number = phoneNumber,
                password = password,
                password_confirm = passwordConfirm,
                age = age,
                gender = gender,
                address = address,
                comments = comments,
                profile_photo = profilePhoto
            )

            val response = authApi.register(request)

            if (response.isSuccessful) {
                val registerResponse = response.body()
                if (registerResponse != null) {
                    Result.success(registerResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Registration failed"
                } catch (e: Exception) {
                    "Registration failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        phoneNumber: String,
        password: String
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(
                phone_number = phoneNumber,
                password = password
            )

            val response = authApi.login(request)

            android.util.Log.d("AuthRepository", "Login response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
            
            if (response.isSuccessful) {
                try {
                    val loginResponse = response.body()
                    android.util.Log.d("AuthRepository", "Response body is null: ${loginResponse == null}")
                    if (loginResponse != null) {
                        android.util.Log.d("AuthRepository", "Login response parsed - success: ${loginResponse.success}, data: ${loginResponse.data != null}")
                        android.util.Log.d("AuthRepository", "Tokens: ${loginResponse.data?.tokens != null}, Access token: ${loginResponse.data?.tokens?.access != null}")
                        Result.success(loginResponse)
                    } else {
                        android.util.Log.e("AuthRepository", "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Error parsing login response", e)
                    Result.failure(Exception("Failed to parse login response: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Login failed"
                } catch (e: Exception) {
                    "Login failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(authToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AuthRepository", "Calling logout API with token: ${authToken.take(20)}...")
            val response = authApi.logout("Bearer $authToken")
            android.util.Log.d("AuthRepository", "Logout response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
            if (response.isSuccessful) {
                android.util.Log.d("AuthRepository", "Logout successful")
                Result.success(Unit)
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Logout failed"
                } catch (e: Exception) {
                    "Logout failed: ${response.code()} - ${response.message()}"
                }
                android.util.Log.e("AuthRepository", "Logout failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Logout exception", e)
            Result.failure(e)
        }
    }
}
