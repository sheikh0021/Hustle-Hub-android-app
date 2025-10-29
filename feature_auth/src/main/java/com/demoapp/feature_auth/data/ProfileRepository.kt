package com.demoapp.feature_auth.data

import android.content.Context
import android.net.Uri
import com.demoapp.core_network.NetworkClient
import com.demoapp.core_network.models.UserProfileResponse
import com.demoapp.core_network.models.ProfileUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileRepository {

    private val profileApi = NetworkClient.profileApi

    suspend fun uploadProfilePhoto(
        firstName: String,
        lastName: String,
        email: String,
        authToken: String,
        imageUri: Uri,
        context: Context
    ): Result<UserProfileResponse> = withContext(Dispatchers.IO) {
        try {
            // Convert Uri to File
            val file = uriToFile(imageUri, context)
            
            // Create RequestBody for text fields
            val firstNameBody = firstName.toRequestBody("text/plain".toMediaType())
            val lastNameBody = lastName.toRequestBody("text/plain".toMediaType())
            val emailBody = email.toRequestBody("text/plain".toMediaType())
            
            // Create MultipartBody.Part for the image
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val profilePhotoPart = MultipartBody.Part.createFormData(
                "profile_photo", 
                file.name, 
                requestFile
            )

            // Make API call to Django backend
            val response = profileApi.updateProfileWithPhoto(
                authorization = "Bearer $authToken",
                firstName = firstNameBody,
                lastName = lastNameBody,
                email = emailBody,
                profilePhoto = profilePhotoPart
            )

            if (response.isSuccessful) {
                val userProfile = response.body()
                if (userProfile != null) {
                    Result.success(userProfile)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileWithoutPhoto(
        firstName: String,
        lastName: String,
        email: String,
        authToken: String,
        context: Context
    ): Result<UserProfileResponse> = withContext(Dispatchers.IO) {
        try {
            // Create request body
            val request = ProfileUpdateRequest(
                first_name = firstName,
                last_name = lastName,
                email = email
            )

            // Make API call to Django backend
            val response = profileApi.updateProfile(
                authorization = "Bearer $authToken",
                request = request
            )

            if (response.isSuccessful) {
                val userProfile = response.body()
                if (userProfile != null) {
                    Result.success(userProfile)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open input stream")
        
        val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        return file
    }
}